package io.github.fusionflux.portalcubed.content.portal.graphics.render;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import io.github.fusionflux.portalcubed.content.portal.graphics.render.stencil.PortalStencilRenderer;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
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
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.impl.client.rendering.WorldRenderContextImpl;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.world.phys.Vec3;

public final class PortalViewRenderer {
	private PortalViewRenderer() {}

	private static final RecursionAttachedResource<RenderBuffers> RENDER_BUFFERS = RecursionAttachedResource.create(() -> new RenderBuffers(1));

	// the fog is coming
	private static Vector4f fogColor;
	private static FogParameters terrainFog;
	private static FogParameters skyFog;

	private static final List<PortalId> viewPortals = new ObjectArrayList<>();

	@Nullable
	public static PortalId getViewPortal() {
		if (viewPortals.isEmpty())
			return null;
		return viewPortals.get(recursion() - 1);
	}

	public static int recursion() {
		return viewPortals.size();
	}

	public static int level() {
		return recursion() + 1;
	}

	public static boolean isPortalView() {
		return recursion() > 0;
	}

	@Nullable
	public static Vector4f updateFogColor(Supplier<Vector4f> supplier) {
		// note: do not call supplier twice, has side effects

		if (!isPortalView()) {
			fogColor = supplier.get();
			return fogColor;
		}

		if (fogColor != null) {
			return fogColor;
		} else {
			return supplier.get();
		}
	}

	public static FogParameters updateTerrainFog(FogParameters fog) {
		if (!isPortalView()) {
			terrainFog = fog;
		}

		return terrainFog != null ? terrainFog : fog;
	}

	public static FogParameters updateSkyFog(FogParameters fog) {
		if (!isPortalView()) {
			skyFog = fog;
		}

		return skyFog != null ? skyFog : fog;
	}

	static void render(List<PortalRenderer.VisiblePortal> visiblePortals, float tickDelta, PoseStack matrices, WorldRenderContext context) {
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		visiblePortals.forEach(visiblePortal -> renderPortal(visiblePortal, tickDelta, matrices, context));
		if (!isPortalView()) {
			GL11.glDisable(GL11.GL_STENCIL_TEST);
			RenderingUtils.defaultStencil();
			RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT);
		} else {
			RenderingUtils.setupStencilToRenderIfValue(recursion());
			RenderSystem.stencilMask(0x00);
		}
	}

	private static void renderPortal(PortalRenderer.VisiblePortal visiblePortal, float tickDelta, PoseStack matrices, WorldRenderContext context) {
		if (!visiblePortal.open())
			return;

		Portal portal = visiblePortal.portal();
		PortalType.Stencil stencilAsset = portal.type().stencil().orElse(null);
		if (stencilAsset == null)
			return;

		Portal linked = Objects.requireNonNull(visiblePortal.linked());
		PortalTransform transform = new SinglePortalTransform(portal, linked);

		Camera camera = context.camera();
		Vec3 camPos = camera.getPosition();
		boolean inside = OBB.extrudeQuad(portal.quad, PortalStencilRenderer.DEPTH).contains(camPos);
		if (!inside && portal.plane.isBehind(camPos))
			return;

		matrices.pushPose();
		Matrix4f matrix = visiblePortal.transform(context.world(), tickDelta, matrices).pose();
		matrices.popPose();

		// Draw stencil
		RenderingUtils.setupStencilForWriting(recursion(), true);
		PortalStencilRenderer.INSTANCE.render(RenderStateShard.LEQUAL_DEPTH_TEST, stencilAsset, inside, matrix);

		viewPortals.add(visiblePortal.id());

		// Render the world
		Matrix4fStack modelViewMatrices = RenderSystem.getModelViewStack();
		modelViewMatrices.pushMatrix();
		modelViewMatrices.identity();
		try (StateCapture ignored = StateCapture.capture(context)) {
			// Setup camera
			Vec3 portalCamPos = transform.applyAbsolute(camPos);
			((CameraAccessor) camera).pc$setPosition(portalCamPos);

			Quaternionf portalCamRot = camera.rotation();
			portalCamRot.premul(portal.rotation().conjugate(new Quaternionf())).premul(linked.rotation180);
			portalCamRot.transform(0, 0, -1, camera.getLookVector());
			portalCamRot.transform(0, 1, 0, camera.getUpVector());
			portalCamRot.transform(-1, 0, 0, camera.getLeftVector());
			Matrix4f viewMatrix = new Matrix4f().rotation(portalCamRot.conjugate(new Quaternionf()));

			linked.plane.getClipping(viewMatrix, portalCamPos, ShaderPatcher.CLIPPING_PLANES[0]);

			GameRenderer gameRenderer = context.gameRenderer();
			LevelRenderer levelRenderer = context.worldRenderer();
			((LevelRendererAccessor) levelRenderer).callPrepareCullFrustum(linked.origin(), viewMatrix, context.projectionMatrix());

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
			viewPortals.removeLast();
			if (!isPortalView()) {
				GL11.glDisable(GL11.GL_CLIP_PLANE0);
				fogColor = null;
				terrainFog = null;
				skyFog = null;
			}

			modelViewMatrices.popMatrix();
			RenderSystem.enableDepthTest();
		}

		// Restore depth
		RenderingUtils.setupStencilForWriting(recursion() + 1, false);
		PortalStencilRenderer.INSTANCE.render(RenderStateShard.NO_DEPTH_TEST, stencilAsset, inside, matrix);
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
}
