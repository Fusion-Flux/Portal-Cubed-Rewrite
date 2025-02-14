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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.content.portal.RecursionAttachedResource;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.framework.render.PortalCubedRenderTypes;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import io.github.fusionflux.portalcubed.mixin.client.CameraAccessor;
import io.github.fusionflux.portalcubed.mixin.client.GameRendererAccessor;
import io.github.fusionflux.portalcubed.mixin.client.LevelRendererAccessor;
import io.github.fusionflux.portalcubed.mixin.client.RenderSectionManagerAccessor;
import io.github.fusionflux.portalcubed.mixin.client.RenderSystemAccessor;
import io.github.fusionflux.portalcubed.mixin.client.SodiumWorldRendererAccessor;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

public class PortalRenderer {
	private static final double OFFSET_FROM_WALL = 0.001;

	private static final RecursionAttachedResource<RenderBuffers> RENDER_BUFFERS = RecursionAttachedResource.create(() -> new RenderBuffers(1));
	public static final RecursionAttachedResource<Vector4f> CLIPPING_PLANES = RecursionAttachedResource.create(Vector4f::new);

	private static int maxRecursions = 5;
	private static int recursion = 0;

	public static int recursion() {
		return recursion;
	}

	public static boolean isRenderingView() {
		return recursion() > 0;
	}

	public static void setMaxRecursions(int maxRecursions) {
		PortalRenderer.maxRecursions = maxRecursions;
		RecursionAttachedResource.cleanup();
	}

	public static boolean shouldRenderView(PortalInstance portal, Camera camera) {
		return recursion < maxRecursions && portal.plane.isInFront(camera);
	}

	private static void render(WorldRenderContext context) {
		ClientPortalManager manager = context.world().portalManager();
		Collection<PortalPair> pairs = manager.getAllPairs();
		if (pairs.isEmpty())
			return;

		// Collect visible portals
		List<VisiblePortal> visiblePortals = new ObjectArrayList<>();
		Frustum frustum = Objects.requireNonNull(context.frustum());
		for (PortalPair pair : pairs) {
			for (PortalInstance portal : pair) {
				boolean inVisibleSection = SodiumWorldRenderer.instance().isBoxVisible(
						portal.renderBounds.minX,
						portal.renderBounds.minY,
						portal.renderBounds.minZ,
						portal.renderBounds.maxX,
						portal.renderBounds.maxY,
						portal.renderBounds.maxZ
				);
				if (inVisibleSection && frustum.isVisible(portal.renderBounds)) {
					PortalInstance linked = pair.other(portal);
					PortalType type = portal.data.type().value();
					boolean render = portal.data.render();
					boolean hasStencil = type.stencil().isPresent();
					visiblePortals.add(new VisiblePortal(pair, portal, linked, render && hasStencil && linked != null));
				}
			}
		}

		if (!visiblePortals.isEmpty()) {
			PoseStack matrices = new PoseStack();
			Vec3 camPos = context.camera().getPosition();
			matrices.translate(-camPos.x, -camPos.y, -camPos.z);

			// Render portal views
			GL11.glEnable(GL11.GL_STENCIL_TEST);
			visiblePortals.forEach(visiblePortal -> renderPortalView(visiblePortal, matrices, context));
			if (!isRenderingView()) {
				GL11.glDisable(GL11.GL_STENCIL_TEST);
				RenderingUtils.defaultStencil();
				RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT);
			} else {
				RenderingUtils.setupStencilToRenderIfValue(recursion);
				RenderSystem.stencilMask(0x00);
			}

			// Render portals
			RenderType renderType = PortalCubedRenderTypes.emissive(PortalTextureManager.ATLAS_LOCATION);
			BufferBuilder vertices = Tesselator.getInstance().begin(renderType.mode(), renderType.format());
			visiblePortals.forEach(visiblePortal -> renderPortal(visiblePortal, matrices, vertices));
			renderType.draw(vertices.buildOrThrow());
		}
	}

	private static PoseStack.Pose transformToPortal(VisiblePortal visiblePortal, PoseStack matrices) {
		PortalInstance portal = visiblePortal.portal;

		// translate to portal pos
		matrices.translate(portal.data.origin());
		// apply rotations
		matrices.mulPose(portal.rotation()); // rotate towards facing direction
		matrices.mulPose(Axis.ZP.rotationDegrees(180));
		// slight offset so origin is center of portal
		matrices.translate(-0.5f, -1, 0);
		// small offset away from the wall to not z-fight
		matrices.translate(0, 0, -OFFSET_FROM_WALL);

		return matrices.last();
	}

	private static void renderPortalStencil(PortalInstance portal, ResourceLocation texture, PoseStack.Pose pose, Camera camera) {
		// Setup state
		BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		RenderSystem.setShader(CoreShaders.POSITION_TEX);
		RenderSystem.setShaderTexture(0, texture);
		boolean clampDepth = portal.renderBounds.contains(camera.getPosition());
		if (clampDepth)
			GL11.glEnable(ARBDepthClamp.GL_DEPTH_CLAMP);
		RenderSystem.colorMask(false, false, false, false);

		// Build quad
		builder.addVertex(pose, 1, 2, 0).setUv(1, 1);
		builder.addVertex(pose, 1, 0, 0).setUv(1, 0);
		builder.addVertex(pose, 0, 0, 0).setUv(0, 0);
		builder.addVertex(pose, 0, 2, 0).setUv(0, 1);

		// Draw quad
		BufferUploader.drawWithShader(builder.buildOrThrow());

		// Cleanup state
		if (clampDepth)
			GL11.glDisable(ARBDepthClamp.GL_DEPTH_CLAMP);
		RenderSystem.colorMask(true, true, true, true);
	}

	private static void renderPortalView(VisiblePortal visiblePortal, PoseStack matrices, WorldRenderContext context) {
		if (!visiblePortal.open)
			return;

		PortalInstance linked = visiblePortal.linked;
		if (linked == null)
			return;

		PortalInstance portal = visiblePortal.portal;
		Camera camera = context.camera();
		if (!shouldRenderView(portal, camera))
			return;

		matrices.pushPose();
		PoseStack.Pose pose = transformToPortal(visiblePortal, matrices);

		// Draw stencil
		//noinspection OptionalGetWithoutIsPresent
		ResourceLocation stencilTexture = portal.data.type().value().stencil().get();
		RenderSystem.depthMask(false);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderingUtils.setupStencilForWriting(recursion, true);
		renderPortalStencil(portal, stencilTexture, pose, camera);
		RenderSystem.depthMask(true);

		// Backup old state
		LevelRenderer worldRenderer = context.worldRenderer();
		StateCapture oldState = StateCapture.capture(worldRenderer, camera);

		recursion++;
		((LevelRendererAccessor) worldRenderer).setRenderBuffers(RENDER_BUFFERS.get());

		// Setup camera
		Vec3 camPos = PortalTeleportHandler.teleportAbsoluteVecBetween(camera.getPosition(), portal, linked);
		((CameraAccessor) camera).pc$setPosition(camPos);

		Quaternionf camRot = camera.rotation();
		camRot.premul(portal.rotation().invert(new Quaternionf())).premul(linked.rotation180);
		camRot.transform(0, 0, -1, camera.getLookVector());
		camRot.transform(0, 1, 0, camera.getUpVector());
		camRot.transform(-1, 0, 0, camera.getLeftVector());
		Matrix4f viewMatrix = new Matrix4f().rotation(camRot.conjugate(new Quaternionf())) ;

		linked.plane.getClipping(viewMatrix, camPos, CLIPPING_PLANES.get());

		GameRenderer gameRenderer = context.gameRenderer();
		((LevelRendererAccessor) worldRenderer).callPrepareCullFrustum(linked.data.origin(), viewMatrix, gameRenderer.getProjectionMatrix(Minecraft.getInstance().options.fov().get()));

		// Render the world
		RenderingUtils.setupStencilToRenderIfValue(recursion);
		RenderSystem.stencilMask(0x00);
		RenderSystemAccessor.setModelViewStack(new Matrix4fStack(16));
		GL11.glEnable(GL11.GL_CLIP_PLANE0);
		worldRenderer.renderLevel(
				((GameRendererAccessor) gameRenderer).getResourcePool(),
				context.tickCounter(),
				false,
				camera,
				gameRenderer,
				viewMatrix,
				context.projectionMatrix()
		);
		GL11.glDisable(GL11.GL_CLIP_PLANE0);

		// Restore old state
		oldState.restore(worldRenderer, camera);
		recursion--;

		// Restore depth
		RenderingUtils.setupStencilForWriting(recursion + 1, false);
		RenderSystem.depthFunc(GL11.GL_ALWAYS);
		RenderSystem.colorMask(false, false, false, false);
		renderPortalStencil(portal, stencilTexture, pose, camera);
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.colorMask(true, true, true, true);

		matrices.popPose();
	}

	private static void renderPortal(VisiblePortal visiblePortal, PoseStack matrices, VertexConsumer vertices) {
		matrices.pushPose();
		PoseStack.Pose pose = transformToPortal(visiblePortal, matrices);

		PortalData portal = visiblePortal.portal.data;
		PortalType.Textures textures = portal.type().value().textures();
		List<PortalType.Textures.Layer> layers = visiblePortal.open ? textures.open() : textures.closed();
		for (PortalType.Textures.Layer layer : layers) {
			TextureAtlasSprite sprite = PortalTextureManager.INSTANCE.getSprite(layer.texture());
			int color = layer.tint() ? ARGB.opaque(portal.color()) : -1;
			RenderingUtils.quadVertex(vertices, pose, LightTexture.FULL_BRIGHT, 1, 2, color, sprite.getU1(), sprite.getV1());
			RenderingUtils.quadVertex(vertices, pose, LightTexture.FULL_BRIGHT, 1, 0, color, sprite.getU1(), sprite.getV0());
			RenderingUtils.quadVertex(vertices, pose, LightTexture.FULL_BRIGHT, 0, 0, color, sprite.getU0(), sprite.getV0());
			RenderingUtils.quadVertex(vertices, pose, LightTexture.FULL_BRIGHT, 0, 2, color, sprite.getU0(), sprite.getV1());
		}

		matrices.popPose();
	}

	private record VisiblePortal(PortalPair pair, PortalInstance portal, @Nullable PortalInstance linked, boolean open) {
	}

	private record StateCapture(
			Matrix4fStack modelViewStack,
			Frustum frustum,
			Vec3 cameraPosition,
			Quaternionf cameraRotation,
			Vector3f cameraLookVector,
			Vector3f cameraUpVector,
			Vector3f cameraLeftVector,
			Vector3f[] shaderLightDirections,
			FogParameters fog,
			RenderBuffers renderBuffers,
			SortedRenderLists renderLists,
			LongArrayList visibleSections
	) {
		public static StateCapture capture(LevelRenderer worldRenderer, Camera camera) {
			RenderSectionManager renderSectionManager = ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager();
			LongArrayList visibleSections = new LongArrayList();
			int frame = ((RenderSectionManagerAccessor) renderSectionManager).getLastUpdatedFrame();
			Long2ReferenceMap<RenderSection> sections = ((RenderSectionManagerAccessor) renderSectionManager).getSectionByPosition();
			for (Long2ReferenceMap.Entry<RenderSection> entry : sections.long2ReferenceEntrySet()) {
				if (entry.getValue().getLastVisibleFrame() == frame)
					visibleSections.add(entry.getLongKey());
			}
			return new StateCapture(
//					RenderSystem.getInverseViewRotationMatrix(),
					RenderSystem.getModelViewStack(),
					((LevelRendererAccessor) worldRenderer).getCullingFrustum(),
					camera.getPosition(),
					new Quaternionf(camera.rotation()),
					new Vector3f(camera.getLookVector()),
					new Vector3f(camera.getUpVector()),
					new Vector3f(camera.getLeftVector()),
					RenderSystemAccessor.getShaderLightDirections().clone(),
					RenderSystem.getShaderFog(),
					((LevelRendererAccessor) worldRenderer).getRenderBuffers(),
					renderSectionManager.getRenderLists(),
					visibleSections
			);
		}

		public void restore(LevelRenderer worldRenderer, Camera camera) {
//			RenderSystem.setInverseViewRotationMatrix(this.inverseViewRotationMatrix);
			RenderSystemAccessor.setModelViewStack(this.modelViewStack);
			((LevelRendererAccessor) worldRenderer).setCullingFrustum(this.frustum);
			((CameraAccessor) camera).pc$setPosition(this.cameraPosition);
			camera.rotation().set(this.cameraRotation);
			camera.getLookVector().set(this.cameraLookVector);
			camera.getUpVector().set(this.cameraUpVector);
			camera.getLeftVector().set(this.cameraLeftVector);
			RenderSystem.setShaderLights(this.shaderLightDirections[0], this.shaderLightDirections[1]);
			RenderSystem.setShaderFog(this.fog);
			((LevelRendererAccessor) worldRenderer).setRenderBuffers(this.renderBuffers);

			RenderSectionManager renderSectionManager = ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager();
			((RenderSectionManagerAccessor) renderSectionManager).setRenderLists(this.renderLists);

			int frame = ((RenderSectionManagerAccessor) renderSectionManager).getLastUpdatedFrame();
			Long2ReferenceMap<RenderSection> sections = ((RenderSectionManagerAccessor) renderSectionManager).getSectionByPosition();
			for (long pos : this.visibleSections) {
				sections.get(pos).setLastVisibleFrame(frame);
			}

			RenderSystem.enableDepthTest();
		}
	}

	public static void init() {
		WorldRenderEvents.BEFORE_DEBUG_RENDER.register(PortalRenderer::render);
		WorldRenderEvents.AFTER_ENTITIES.register(PortalDebugRenderer::render);
	}
}
