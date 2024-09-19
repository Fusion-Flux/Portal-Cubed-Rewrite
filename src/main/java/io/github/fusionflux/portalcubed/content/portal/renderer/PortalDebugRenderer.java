package io.github.fusionflux.portalcubed.content.portal.renderer;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PortalDebugRenderer {
	static void render(WorldRenderContext context) {
		if (!Minecraft.getInstance().getDebugOverlay().showDebugScreen())
			return;

		for (PortalPair pair : context.world().portalManager().getAllPairs()) {
			if (!pair.isLinked())
				continue;

			for (PortalInstance portal : pair) {
				renderPortalDebug(portal, pair.other(portal), context, context.matrixStack(), context.consumers());
			}
		}
	}


	private static void renderPortalDebug(PortalInstance portal, PortalInstance linked, WorldRenderContext ctx, PoseStack matrices, MultiBufferSource vertexConsumers) {
		matrices.pushPose();
		Camera camera = ctx.camera();
		matrices.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

		// origin and quad
		AABB originBox = AABB.ofSize(portal.data.origin(), 0.1, 0.1, 0.1);
		RenderingUtils.renderBox(matrices, vertexConsumers, originBox, Color.GREEN);
		RenderingUtils.renderQuad(matrices, vertexConsumers, portal.quad, Color.GREEN);
		RenderingUtils.renderVec(matrices, vertexConsumers, portal.quad.normal(), portal.data.origin(), Color.RED);
		RenderingUtils.renderVec(matrices, vertexConsumers, portal.quad.up(), portal.data.origin(), Color.BLUE);
		// collision bounds
		RenderingUtils.renderBox(matrices, vertexConsumers, portal.entityCollisionBounds, Color.RED);
		RenderingUtils.renderBox(matrices, vertexConsumers, portal.blockModificationArea, Color.PURPLE);
		portal.blockModificationShapes.forEach(($, shape) -> RenderingUtils.renderShape(matrices, vertexConsumers, shape, Color.CYAN));
		// cross-portal collision
//		renderCollision(ctx, portal, linked);
		// render player's raycast through
		Vec3 pos = camera.getPosition();
		Vector3f lookVector = camera.getLookVector().normalize(3, new Vector3f());
		Vec3 end = pos.add(lookVector.x, lookVector.y, lookVector.z);
		PortalHitResult hit = ctx.world().portalManager().activePortals().clip(pos, end);
		if (hit != null) {
			// start -> hitIn
			RenderingUtils.renderLine(matrices, vertexConsumers, hit.start(), hit.inHit(), Color.ORANGE);
			// box at hitIn
			AABB hitInBox = AABB.ofSize(hit.inHit(), 0.1, 0.1, 0.1);
			RenderingUtils.renderBox(matrices, vertexConsumers, hitInBox, Color.ORANGE);
			// box at hitOut
			AABB hitOutBox = AABB.ofSize(hit.outHit(), 0.1, 0.1, 0.1);
			RenderingUtils.renderBox(matrices, vertexConsumers, hitOutBox, Color.BLUE);

			if (hit.isEnd()) {
				// hitOut -> end
				RenderingUtils.renderLine(matrices, vertexConsumers, hit.outHit(), hit.end(), Color.BLUE);
				// box at end
				AABB endBox = AABB.ofSize(hit.end(), 0.1, 0.1, 0.1);
				RenderingUtils.renderBox(matrices, vertexConsumers, endBox, Color.BLUE);
			}
		}
		matrices.popPose();
	}

//	private static void renderCollision(WorldRenderContext ctx, PortalInstance portal, PortalInstance linked) {
//		Camera camera = ctx.camera();
//		Entity entity = camera.getEntity();
//		ClientLevel level = ctx.world();
//		PoseStack matrices = ctx.matrixStack();
//		VertexConsumer vertices = Objects.requireNonNull(ctx.consumers()).getBuffer(RenderType.lines());
//
//		List<VoxelShape> shapes = VoxelShenanigans.getShapesBehindPortal(level, entity, portal, linked);
//		shapes.forEach(shape -> LevelRenderer.renderVoxelShape(
//				matrices, vertices, shape,
//				0, 0, 0,
//				1, 1, 1, 1,
//				true
//		));
//	}
}
