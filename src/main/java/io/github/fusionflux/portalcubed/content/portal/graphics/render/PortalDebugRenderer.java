package io.github.fusionflux.portalcubed.content.portal.graphics.render;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.PortalCubedClient;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.framework.extension.Vec3Ext;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

public class PortalDebugRenderer {
	static void render(WorldRenderContext context) {
		if (!PortalCubedClient.portalDebugEnabled)
			return;

		for (PortalPair pair : context.world().portalManager().getAllPairs()) {
			if (!pair.isLinked())
				continue;

			renderPortalDebug(pair, Polarity.PRIMARY, context, context.matrixStack(), context.consumers());
			renderPortalDebug(pair, Polarity.SECONDARY, context, context.matrixStack(), context.consumers());
		}
	}

	private static void renderPortalDebug(PortalPair pair, Polarity polarity, WorldRenderContext ctx, PoseStack matrices, MultiBufferSource buffers) {
		Portal portal = pair.getOrThrow(polarity);
		Portal linked = pair.getOrThrow(polarity.opposite());
		renderPortalDebug(portal, polarity, linked, ctx, matrices, buffers);
	}

	private static void renderPortalDebug(Portal portal, Polarity polarity, Portal linked, WorldRenderContext ctx, PoseStack matrices, MultiBufferSource buffers) {
		matrices.pushPose();
		Camera camera = ctx.camera();
		matrices.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

		// origin and quad
		AABB originBox = AABB.ofSize(portal.data.origin(), 0.1, 0.1, 0.1);
		RenderingUtils.renderBox(matrices, buffers, originBox, Color.GREEN);
		RenderingUtils.renderQuad(matrices, buffers, portal.quad, Color.GREEN);
		RenderingUtils.renderVec(matrices, buffers, portal.normal, portal.data.origin(), Color.RED);
		RenderingUtils.renderVec(matrices, buffers, Vec3Ext.of(portal.quad.up()), portal.data.origin(), Color.BLUE);
		// plane
		RenderingUtils.renderPlane(matrices, buffers, portal.plane, 2.5f, Color.ORANGE);
		// collision bounds
		portal.perimeterBoxes.forEach(shape -> {
			Color color = shape.intersects(camera.getEntity().getBoundingBox()) ? Color.RED : Color.CYAN;
			RenderingUtils.renderBox(matrices, buffers, shape, color);
		});
		//RenderingUtils.renderBox(matrices, buffers, portal.entityCollisionArea, Color.YELLOW);
		//RenderingUtils.renderBox(matrices, buffers, portal.entityCollisionArea.encompassingAabb, Color.YELLOW);
		matrices.popPose();
	}
}
