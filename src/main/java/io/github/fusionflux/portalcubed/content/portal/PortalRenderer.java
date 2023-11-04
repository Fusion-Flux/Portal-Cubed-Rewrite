package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtil;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PortalRenderer {
	private static void render(WorldRenderContext context) {
		if (!(context.consumers() instanceof final MultiBufferSource.BufferSource vertexConsumers))
			return;
		ClientPortalManager manager = ClientPortalManager.of(context.world());
		List<Portal> portals = manager.allPortals();
		if (portals.isEmpty())
			return;

		PoseStack matrices = context.matrixStack();
		Vec3 camPos = context.camera().getPosition();
		matrices.pushPose();
		matrices.translate(-camPos.x, -camPos.y, -camPos.z);
		for (Portal portal : portals) {
			renderPortal(portal, matrices, vertexConsumers);
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
		matrices.mulPose(portal.orientation.front().getRotation());
//		matrices.mulPose(portal.orientation.top().getRotation());
		matrices.mulPose(Axis.XN.rotationDegrees(90));
		// slight offset so origin is center of portal
		matrices.translate(-0.5f, -1f, 0f);
		// scale quad
		matrices.scale(2, 2, 2);
		RenderingUtil.renderQuad(matrices, vertices, LightTexture.FULL_BRIGHT, portal.color);
		matrices.popPose();
	}

	public static void init() {
		WorldRenderEvents.AFTER_ENTITIES.register(PortalRenderer::render);
	}
}
