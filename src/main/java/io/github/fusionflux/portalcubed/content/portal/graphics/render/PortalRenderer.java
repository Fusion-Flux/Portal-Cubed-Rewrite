package io.github.fusionflux.portalcubed.content.portal.graphics.render;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL14C;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import io.github.fusionflux.portalcubed.content.portal.graphics.render.stencil.PortalStencilRenderer;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import io.github.fusionflux.portalcubed.framework.config.PortalCubedClientConfig;
import io.github.fusionflux.portalcubed.framework.extension.RenderBuffersExt;
import io.github.fusionflux.portalcubed.framework.render.PortalCubedRenderTypes;
import io.github.fusionflux.portalcubed.framework.shape.Plane;
import io.github.fusionflux.portalcubed.framework.util.ClientTicks;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import io.github.fusionflux.portalcubed.mixin.client.LevelRendererAccessor;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PortalRenderer {
	public static final double OFFSET_FROM_WALL = 0.001;
	public static final float BASE_TRACER_OPACITY = 180 / 255f;
	public static final double TRACER_FADEOUT_START_DISTANCE = 3.5;
	public static final double TRACER_FADEOUT_END_DISTANCE = 1.5;

	/// With too many recursions we max out [RenderSystem#modelViewStack]
	@SuppressWarnings("JavadocReference")
	public static final int MAX_LEVELS = 8;

	private PortalRenderer() {}

	public static AABB getRenderBounds(Portal portal) {
		return portal.renderBounds.expandTowards(portal.normal.scale(-Math.abs(PortalStencilRenderer.DEPTH)));
	}

	public static boolean isPortalVisible(Frustum frustum, Portal portal) {
		if (!frustum.isVisible(getRenderBounds(portal)))
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
		Collection<PortalReference> portals = manager.portals();
		if (portals.isEmpty())
			return;

		// Collect visible portals
		List<VisiblePortal> visiblePortals = new ReferenceArrayList<>();
		Frustum frustum = Objects.requireNonNull(context.frustum());
		for (PortalReference reference : portals) {
			Portal portal = reference.get();
			if (isPortalVisible(frustum, portal)) {
				Portal linked = reference.opposite().map(PortalReference::get).orElse(null);
				visiblePortals.add(new VisiblePortal(reference.id, portal, linked, isOpen(portal, linked)));
			}
		}

		if (visiblePortals.isEmpty())
			return;

		PoseStack matrices = new PoseStack();
		Vec3 camPos = context.camera().getPosition();
		matrices.translate(-camPos.x, -camPos.y, -camPos.z);

		float tickDelta = context.tickCounter().getGameTimeDeltaPartialTick(false);

		RenderType tracerRenderType = PortalCubedRenderTypes.tracer(PortalTextureManager.ATLAS_LOCATION);
		RenderType renderType = PortalCubedRenderTypes.emissive(PortalTextureManager.ATLAS_LOCATION);
		RenderBuffersExt renderBuffers = (RenderBuffersExt) ((LevelRendererAccessor) context.worldRenderer()).getRenderBuffers();
		BufferBuilder bufferBuilder = new BufferBuilder(renderBuffers.pc$portalByteBufferBuilder(), renderType.mode(), renderType.format());

		// Buffer portals and render tracers
		visiblePortals.forEach(visiblePortal -> {
			BufferBuilder tracerBufferBuilder = new BufferBuilder(renderBuffers.pc$portalTracerByteBufferBuilder(), tracerRenderType.mode(), tracerRenderType.format());
			renderPortal(visiblePortal, matrices, level, tickDelta, bufferBuilder, tracerBufferBuilder);
			try (MeshData mesh = tracerBufferBuilder.build()) {
				if (mesh != null) {
					float alpha = getPortalTracerAlpha(visiblePortal.portal.plane, camPos);
					if (alpha > 0) {
						GL14C.glBlendColor(0, 0, 0, alpha);
						RenderingUtils.renderMesh(mesh, tracerRenderType, renderBuffers.pc$portalTracerByteBufferBuilder());
					}
				}
			}
		});

		PortalViewRenderer.render(visiblePortals, tickDelta, matrices, context);

		// Render portals
		try (MeshData mesh = bufferBuilder.buildOrThrow()) {
			RenderingUtils.renderMesh(mesh, renderType, renderBuffers.pc$portalByteBufferBuilder());
		}
	}

	private static boolean isOpen(Portal portal, @Nullable Portal linked) {
		if (linked == null)
			return false;

		// ignore recursion limit when there's no stencil
		if (portal.type().stencil().isEmpty())
			return true;

		return portal.data.render() && PortalViewRenderer.level() <= maxLevels();
	}

	private static float getPortalTracerAlpha(Plane portalPlane, Vec3 camPos) {
		if (portalPlane.isBehind(camPos))
			return BASE_TRACER_OPACITY;

		return BASE_TRACER_OPACITY * (float) Math.min((camPos.distanceTo(portalPlane.origin()) - TRACER_FADEOUT_END_DISTANCE) / TRACER_FADEOUT_START_DISTANCE, 1);
	}

	private static void renderPortal(VisiblePortal visiblePortal, PoseStack matrices, ClientLevel level, float tickDelta, VertexConsumer vertices, VertexConsumer tracerVertices) {
		matrices.pushPose();
		visiblePortal.transform(level, tickDelta, matrices);

		PortalData portal = visiblePortal.portal.data;
		PortalType.Textures textures = portal.type().value().textures();
		int portalColor = portal.color().getOpaque(ClientTicks.get());

		renderTexture(visiblePortal.open ? textures.open() : textures.closed(), portalColor, matrices, vertices);

		if (visiblePortal.portal.data.tracer()) {
			renderTexture(textures.tracer(), portalColor, matrices, tracerVertices);
		}

		matrices.popPose();
	}

	private static void renderTexture(List<PortalType.Textures.Layer> layers, int portalColor, PoseStack matrices, VertexConsumer vertices) {
		for (PortalType.Textures.Layer layer : layers) {
			matrices.pushPose();
			matrices.translate(0, layer.offset(), 0);
			PoseStack.Pose pose = matrices.last();

			TextureAtlasSprite sprite = PortalTextureManager.INSTANCE.getSprite(layer.texture());
			int color = layer.tint() ? portalColor : -1;

			// start bottom left, go CCW
			RenderingUtils.quadVertex(vertices, pose, LightTexture.FULL_BRIGHT, 1, 0, color, sprite.getU0(), sprite.getV1());
			RenderingUtils.quadVertex(vertices, pose, LightTexture.FULL_BRIGHT, 0, 0, color, sprite.getU1(), sprite.getV1());
			RenderingUtils.quadVertex(vertices, pose, LightTexture.FULL_BRIGHT, 0, 2, color, sprite.getU1(), sprite.getV0());
			RenderingUtils.quadVertex(vertices, pose, LightTexture.FULL_BRIGHT, 1, 2, color, sprite.getU0(), sprite.getV0());

			matrices.popPose();
		}
	}

	private static int maxLevels() {
		return PortalCubedClientConfig.get().portalRenderingLevels();
	}

	record VisiblePortal(PortalId id, Portal portal, @Nullable Portal linked, boolean open) {
		public PoseStack.Pose transform(ClientLevel level, float tickDelta, PoseStack matrices) {
			// translate to portal pos
			matrices.translate(this.portal.origin());
			// apply rotations
			matrices.mulPose(this.portal.rotation()); // rotate towards facing direction
			// animate placement
			PortalType.PlaceAnimation placeAnimation = this.portal.type().placeAnimation();
			float animationProgress = placeAnimation.getProgress(level, this.portal, tickDelta);
			placeAnimation.type().applyPose(animationProgress, matrices);
			// slight offset so origin is center of portal
			matrices.translate(-0.5f, 0, -1);
			// small offset away from the wall to not z-fight
			matrices.translate(0, OFFSET_FROM_WALL, 0);

			return matrices.last();
		}
	}

	public static void init() {
		WorldRenderEvents.BEFORE_DEBUG_RENDER.register(PortalRenderer::render);
		WorldRenderEvents.AFTER_ENTITIES.register(PortalDebugRenderer::render);
		PortalCubedClientConfig.onChange(ignored -> RecursionAttachedResource.cleanup());
	}
}
