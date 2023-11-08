package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import net.minecraft.client.Minecraft;

import net.minecraft.client.renderer.LevelRenderer;

import net.minecraft.client.renderer.culling.Frustum;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PortalRenderer {
	public static final Color PLANE_COLOR = new Color(1, 1, 1, 1);
	public static final Color ACTIVE_PLANE_COLOR = new Color(0.5f, 1, 0.5f, 1);
	public static final Color HOLE_COLOR = new Color(1, 0.5f, 0.5f, 1);

	private static void render(WorldRenderContext context) {
		if (!(context.consumers() instanceof final MultiBufferSource.BufferSource vertexConsumers))
			return;
		ClientPortalManager manager = ClientPortalManager.of(context.world());
		List<Portal> portals = manager.allPortals();
		if (portals.isEmpty())
			return;

		PoseStack matrices = context.matrixStack();
		Vec3 camPos = context.camera().getPosition();
		Frustum frustum = context.frustum();
		matrices.pushPose();
		boolean renderDebug = Minecraft.getInstance().options.renderDebug;
		matrices.translate(-camPos.x, -camPos.y, -camPos.z);
		for (Portal portal : portals) {
			if (frustum.isVisible(portal.plane)) {
				renderPortal(portal, matrices, vertexConsumers);
				if (renderDebug) {
					renderPortalDebug(portal, matrices, vertexConsumers);
				}
			}
		}

		matrices.popPose();
		vertexConsumers.endLastBatch();
	}

	private static void renderPortal(Portal portal, PoseStack matrices, MultiBufferSource vertexConsumers) {
		RenderType renderType = RenderType.beaconBeam(portal.shape.texture, true);
		VertexConsumer vertices = vertexConsumers.getBuffer(renderType);
		matrices.pushPose();
		// translate to portal pos
		matrices.translate(portal.origin.x, portal.origin.y, portal.origin.z);
		// apply rotations
		matrices.mulPose(portal.rotation); // rotate towards facing direction
		matrices.mulPose(Axis.ZP.rotationDegrees(180));
		// slight offset so origin is center of portal
		matrices.translate(-0.5f, -1, 0);
		// scale quad - 32x32 texture, half is used. scale the 1x1 to a 2x2.
		matrices.scale(2, 2, 2);
		RenderingUtils.renderQuad(matrices, vertices, LightTexture.FULL_BRIGHT, portal.color);
		matrices.popPose();
	}

	private static void renderPortalDebug(Portal portal, PoseStack matrices, MultiBufferSource vertexConsumers) {
		// render a box around the portal's plane
		Color planeColor = portal.isActive() ? ACTIVE_PLANE_COLOR : PLANE_COLOR;
		renderBox(matrices, vertexConsumers, portal.plane, planeColor);
		// and the portal's hole
//		renderBox(matrices, vertexConsumers, portal.holeBox, HOLE_COLOR);
	}

	private static void renderBox(PoseStack matrices, MultiBufferSource vertexConsumers, AABB box, Color color) {
		VertexConsumer vertices = vertexConsumers.getBuffer(RenderType.lines());
		LevelRenderer.renderLineBox(matrices, vertices, box, color.r(), color.g(), color.b(), color.a());
	}

	public static void init() {
		WorldRenderEvents.AFTER_ENTITIES.register(PortalRenderer::render);
	}
}
