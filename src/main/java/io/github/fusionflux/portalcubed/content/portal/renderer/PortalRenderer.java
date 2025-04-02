package io.github.fusionflux.portalcubed.content.portal.renderer;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.ARBDepthClamp;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.framework.render.PortalCubedRenderTypes;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import io.github.fusionflux.portalcubed.framework.util.ShaderPatcher;
import io.github.fusionflux.portalcubed.mixin.client.CameraAccessor;
import io.github.fusionflux.portalcubed.mixin.client.GameRendererAccessor;
import io.github.fusionflux.portalcubed.mixin.client.LevelRendererAccessor;
import io.github.fusionflux.portalcubed.mixin.client.RenderSectionManagerAccessor;
import io.github.fusionflux.portalcubed.mixin.client.RenderSystemAccessor;
import io.github.fusionflux.portalcubed.mixin.client.SodiumWorldRendererAccessor;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.impl.client.rendering.WorldRenderContextImpl;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

public class PortalRenderer {
	public static final double OFFSET_FROM_WALL = 0.001;

	private static final RecursionAttachedResource<RenderBuffers> RENDER_BUFFERS = RecursionAttachedResource.create(() -> new RenderBuffers(1));
	private static final ByteBufferBuilder PORTAL_BYTE_BUFFER_BUILDER = new ByteBufferBuilder(RenderType.TRANSIENT_BUFFER_SIZE);

	private static VertexBuffer stencilQuadBuffer;

	private static final List<PortalInstance> renderingPortals = new ObjectArrayList<>();
	private static int maxRecursions = 3;

	@Nullable
	public static PortalInstance getRenderingPortal() {
		if (renderingPortals.isEmpty())
			return null;
		return renderingPortals.get(recursion() - 1);
	}

	public static int recursion() {
		return renderingPortals.size();
	}

	public static boolean isRenderingView() {
		return recursion() > 0;
	}

	public static void setMaxRecursions(int maxRecursions) {
		PortalRenderer.maxRecursions = maxRecursions;
		RecursionAttachedResource.cleanup();
	}

	public static boolean isPortalVisible(Frustum frustum, PortalInstance portal) {
		if (!frustum.isVisible(portal.renderBounds))
			return false;

		return SodiumWorldRenderer.instance().isBoxVisible(
				portal.renderBounds.minX,
				portal.renderBounds.minY,
				portal.renderBounds.minZ,
				portal.renderBounds.maxX,
				portal.renderBounds.maxY,
				portal.renderBounds.maxZ
		);
	}

	private static void render(WorldRenderContext context) {
		ClientLevel level = context.world();
		ClientPortalManager manager = level.portalManager();
		Collection<PortalPair> pairs = manager.getAllPairs();
		if (pairs.isEmpty())
			return;

		// Collect visible portals
		List<VisiblePortal> visiblePortals = new ReferenceArrayList<>();
		Frustum frustum = Objects.requireNonNull(context.frustum());
		for (PortalPair pair : pairs) {
			for (PortalInstance portal : pair) {
				if (isPortalVisible(frustum, portal)) {
					PortalInstance linked = pair.other(portal);
					boolean render = portal.data.render();
					boolean hasStencil = portal.type().stencil().isPresent();
					visiblePortals.add(new VisiblePortal(pair, portal, linked, linked != null && (!hasStencil || (render && recursion() < maxRecursions))));
				}
			}
		}

		if (visiblePortals.isEmpty())
			return;

		PoseStack matrices = new PoseStack();
		Vec3 camPos = context.camera().getPosition();
		matrices.translate(-camPos.x, -camPos.y, -camPos.z);

		float tickDelta = context.tickCounter().getGameTimeDeltaPartialTick(false);

		// Render portal views
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		visiblePortals.forEach(visiblePortal -> renderPortalView(visiblePortal, tickDelta, matrices, context));
		if (!isRenderingView()) {
			GL11.glDisable(GL11.GL_STENCIL_TEST);
			RenderingUtils.defaultStencil();
			RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT);
		} else {
			RenderingUtils.setupStencilToRenderIfValue(recursion());
			RenderSystem.stencilMask(0x00);
		}

		// Render portals
		RenderType renderType = PortalCubedRenderTypes.emissive(PortalTextureManager.ATLAS_LOCATION);
		BufferBuilder bufferBuilder = new BufferBuilder(PORTAL_BYTE_BUFFER_BUILDER, renderType.mode(), renderType.format());
		visiblePortals.forEach(visiblePortal -> renderPortal(visiblePortal, matrices, level, tickDelta, bufferBuilder));
		try (MeshData mesh = bufferBuilder.buildOrThrow()) {
			if (renderType.sortOnUpload())
				mesh.sortQuads(PORTAL_BYTE_BUFFER_BUILDER, RenderSystem.getProjectionType().vertexSorting());
			renderType.draw(mesh);
		}
	}

	private static PoseStack.Pose transformToPortal(VisiblePortal visiblePortal, ClientLevel level, float tickDelta, PoseStack matrices) {
		PortalInstance portal = visiblePortal.portal;

		// translate to portal pos
		matrices.translate(portal.data.origin());
		// apply rotations
		matrices.mulPose(portal.rotation()); // rotate towards facing direction
		// animate placement
		PortalType.PlaceAnimation placeAnimation = portal.type().placeAnimation();
		float animationProgress = placeAnimation.getProgress(level, portal, tickDelta);
		placeAnimation.type().applyPose(animationProgress, matrices);
		// slight offset so origin is center of portal
		matrices.translate(-0.5f, 0, -1);
		// small offset away from the wall to not z-fight
		matrices.translate(0, OFFSET_FROM_WALL, 0);

		return matrices.last();
	}

	private static void renderPortalStencil(RenderStateShard.DepthTestStateShard depthTest, ResourceLocation texture, Matrix4f matrix) {
		if (stencilQuadBuffer == null) {
			try (ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(DefaultVertexFormat.POSITION_TEX.getVertexSize() * 4)) {
				BufferBuilder builder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

				builder.addVertex(1, 0, 0).setUv(0, 1);
				builder.addVertex(0, 0, 0).setUv(1, 1);
				builder.addVertex(0, 0, 2).setUv(1, 0);
				builder.addVertex(1, 0, 2).setUv(0, 0);

				try (MeshData mesh = builder.buildOrThrow()) {
					stencilQuadBuffer = new VertexBuffer(BufferUsage.STATIC_WRITE);
					stencilQuadBuffer.bind();
					stencilQuadBuffer.upload(mesh);
					VertexBuffer.unbind();
				}
			}
		}

		GL11.glEnable(ARBDepthClamp.GL_DEPTH_CLAMP);

		Matrix4fStack matrices = RenderSystem.getModelViewStack();
		matrices.pushMatrix();
		matrices.mul(matrix);
		stencilQuadBuffer.drawWithRenderType(PortalCubedRenderTypes.depthCutout(depthTest, texture));
		matrices.popMatrix();

		GL11.glDisable(ARBDepthClamp.GL_DEPTH_CLAMP);
		RenderSystem.enableDepthTest();
	}

	private static void renderPortalView(VisiblePortal visiblePortal, float tickDelta, PoseStack matrices, WorldRenderContext context) {
		if (!visiblePortal.open)
			return;

		PortalInstance portal = visiblePortal.portal;
		ResourceLocation stencilTexture = portal.type().stencil()
				.map(id -> id.withPath(path -> "textures/" + path + ".png"))
				.orElse(null);
		if (stencilTexture == null)
			return;

		PortalInstance linked = visiblePortal.linked;
		if (linked == null)
			return;

		Camera camera = context.camera();
		if (!portal.plane.isInFront(camera))
			return;

		matrices.pushPose();
		Matrix4f matrix = transformToPortal(visiblePortal, context.world(), tickDelta, matrices).pose();
		matrices.popPose();

		// Draw stencil
		RenderingUtils.setupStencilForWriting(recursion(), true);
		renderPortalStencil(RenderStateShard.LEQUAL_DEPTH_TEST, stencilTexture, matrix);

		renderingPortals.add(portal);

		// Render the world
		Matrix4fStack modelViewMatrices = RenderSystem.getModelViewStack();
		modelViewMatrices.pushMatrix();
		modelViewMatrices.identity();
		try (StateCapture ignored = StateCapture.capture(context)) {
			// Setup camera
			Vec3 camPos = PortalTeleportHandler.teleportAbsoluteVecBetween(camera.getPosition(), portal, linked);
			((CameraAccessor) camera).pc$setPosition(camPos);

			Quaternionf camRot = camera.rotation();
			camRot.premul(portal.rotation().conjugate(new Quaternionf())).premul(linked.rotation180);
			camRot.transform(0, 0, -1, camera.getLookVector());
			camRot.transform(0, 1, 0, camera.getUpVector());
			camRot.transform(-1, 0, 0, camera.getLeftVector());
			Matrix4f viewMatrix = new Matrix4f().rotation(camRot.conjugate(new Quaternionf()));

			linked.plane.getClipping(viewMatrix, camPos, ShaderPatcher.CLIPPING_PLANES[0]);

			GameRenderer gameRenderer = context.gameRenderer();
			LevelRenderer levelRenderer = context.worldRenderer();
			((LevelRendererAccessor) levelRenderer).callPrepareCullFrustum(linked.data.origin(), viewMatrix, context.projectionMatrix());

			RenderingUtils.setupStencilToRenderIfValue(recursion());
			RenderSystem.stencilMask(0x00);
			((LevelRendererAccessor) levelRenderer).setRenderBuffers(RENDER_BUFFERS.get());
			WorldRenderContextHook.INSTANCE.set(levelRenderer, new WorldRenderContextImpl());
			GL11.glEnable(GL11.GL_CLIP_PLANE0);
			levelRenderer.renderLevel(
					((GameRendererAccessor) gameRenderer).getResourcePool(),
					context.tickCounter(),
					context.blockOutlines(),
					camera,
					gameRenderer,
					viewMatrix,
					context.projectionMatrix()
			);
		} finally {
			renderingPortals.removeLast();
			if (!isRenderingView())
				GL11.glDisable(GL11.GL_CLIP_PLANE0);

			modelViewMatrices.popMatrix();
			RenderSystem.enableDepthTest();
		}

		// Restore depth
		RenderingUtils.setupStencilForWriting(recursion() + 1, false);
		RenderSystem.depthFunc(GL11.GL_ALWAYS);
		renderPortalStencil(RenderStateShard.NO_DEPTH_TEST, stencilTexture, matrix);
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
	}

	private static void renderPortal(VisiblePortal visiblePortal, PoseStack matrices, ClientLevel level, float tickDelta, VertexConsumer vertices) {
		matrices.pushPose();
		transformToPortal(visiblePortal, level, tickDelta, matrices);

		PortalData portal = visiblePortal.portal.data;
		PortalType.Textures textures = portal.type().value().textures();
		List<PortalType.Textures.Layer> layers = visiblePortal.open ? textures.open() : textures.closed();
		for (PortalType.Textures.Layer layer : layers) {
			matrices.pushPose();
			matrices.translate(0, layer.offset(), 0);
			PoseStack.Pose pose = matrices.last();

			TextureAtlasSprite sprite = PortalTextureManager.INSTANCE.getSprite(layer.texture());
			int color = layer.tint() ? ARGB.opaque(portal.color()) : -1;

			// start bottom left, go CCW
			RenderingUtils.quadVertex(vertices, pose, LightTexture.FULL_BRIGHT, 1, 0, color, sprite.getU0(), sprite.getV1());
			RenderingUtils.quadVertex(vertices, pose, LightTexture.FULL_BRIGHT, 0, 0, color, sprite.getU1(), sprite.getV1());
			RenderingUtils.quadVertex(vertices, pose, LightTexture.FULL_BRIGHT, 0, 2, color, sprite.getU1(), sprite.getV0());
			RenderingUtils.quadVertex(vertices, pose, LightTexture.FULL_BRIGHT, 1, 2, color, sprite.getU0(), sprite.getV0());

			matrices.popPose();
		}

		matrices.popPose();
	}

	private record VisiblePortal(PortalPair pair, PortalInstance portal, @Nullable PortalInstance linked, boolean open) {
	}

	private record StateCapture(
			Vec3 cameraPosition,
			Quaternionf cameraRotation,
			Vector3f cameraLookVector,
			Vector3f cameraUpVector,
			Vector3f cameraLeftVector,
			Vector4f clippingPlane,
			Vector3f[] shaderLightDirections,
			FogParameters fog,
			RenderBuffers renderBuffers,
			LongList visibleSections,
			WorldRenderContext context
	) implements AutoCloseable {
		public static StateCapture capture(WorldRenderContext context) {
			Camera camera = context.camera();
			RenderSectionManager renderSectionManager = ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager();
			LongArrayList visibleSections = new LongArrayList();
			int frame = ((RenderSectionManagerAccessor) renderSectionManager).getLastUpdatedFrame();
			Long2ReferenceMap<RenderSection> sections = ((RenderSectionManagerAccessor) renderSectionManager).getSectionByPosition();
			for (Long2ReferenceMap.Entry<RenderSection> entry : sections.long2ReferenceEntrySet()) {
				if (entry.getValue().getLastVisibleFrame() == frame)
					visibleSections.add(entry.getLongKey());
			}
			return new StateCapture(
					camera.getPosition(),
					new Quaternionf(camera.rotation()),
					new Vector3f(camera.getLookVector()),
					new Vector3f(camera.getUpVector()),
					new Vector3f(camera.getLeftVector()),
					new Vector4f(ShaderPatcher.CLIPPING_PLANES[0]),
					RenderSystemAccessor.getShaderLightDirections().clone(),
					RenderSystem.getShaderFog(),
					((LevelRendererAccessor) context.worldRenderer()).getRenderBuffers(),
					visibleSections,
					context
			);
		}

		public void restore() {
			LevelRenderer levelRenderer = this.context.worldRenderer();
			WorldRenderContextHook.INSTANCE.set(levelRenderer, this.context);
			((LevelRendererAccessor) levelRenderer).setCullingFrustum(this.context.frustum());
			((LevelRendererAccessor) levelRenderer).setRenderBuffers(this.renderBuffers);

			Camera camera = this.context.camera();
			((CameraAccessor) camera).pc$setPosition(this.cameraPosition);
			camera.rotation().set(this.cameraRotation);
			camera.getLookVector().set(this.cameraLookVector);
			camera.getUpVector().set(this.cameraUpVector);
			camera.getLeftVector().set(this.cameraLeftVector);

			RenderSectionManager renderSectionManager = ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager();
			int frame = ((RenderSectionManagerAccessor) renderSectionManager).getLastUpdatedFrame();
			Long2ReferenceMap<RenderSection> sections = ((RenderSectionManagerAccessor) renderSectionManager).getSectionByPosition();
			for (long pos : this.visibleSections) {
				sections.get(pos).setLastVisibleFrame(frame);
			}

			ShaderPatcher.CLIPPING_PLANES[0].set(this.clippingPlane);
			RenderSystem.setShaderLights(this.shaderLightDirections[0], this.shaderLightDirections[1]);
			RenderSystem.setShaderFog(this.fog);
		}

		@Override
		public void close() {
			this.restore();
		}
	}

	public static void init() {
		WorldRenderEvents.BEFORE_DEBUG_RENDER.register(PortalRenderer::render);
		WorldRenderEvents.AFTER_ENTITIES.register(PortalDebugRenderer::render);
	}
}
