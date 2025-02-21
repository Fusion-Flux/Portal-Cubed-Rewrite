package io.github.fusionflux.portalcubed.content.portal.renderer;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.PortalCubedClient;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalTransform;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
		PortalInstance portal = pair.getOrThrow(polarity);
		PortalInstance linked = pair.getOrThrow(polarity.opposite());
		renderPortalDebug(portal, polarity, linked, ctx, matrices, buffers);
	}

	private static void renderPortalDebug(PortalInstance portal, Polarity polarity, PortalInstance linked, WorldRenderContext ctx, PoseStack matrices, MultiBufferSource buffers) {
		matrices.pushPose();
		Camera camera = ctx.camera();
		matrices.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

		// origin and quad
		AABB originBox = AABB.ofSize(portal.data.origin(), 0.1, 0.1, 0.1);
		RenderingUtils.renderBox(matrices, buffers, originBox, Color.GREEN);
		RenderingUtils.renderQuad(matrices, buffers, portal.quad, Color.GREEN);
		RenderingUtils.renderVec(matrices, buffers, portal.normal, portal.data.origin(), Color.RED);
		RenderingUtils.renderVec(matrices, buffers, portal.quad.up(), portal.data.origin(), Color.BLUE);
		// transform
		if (polarity == Polarity.PRIMARY) {
			renderTransform(portal, linked, matrices, buffers);
		}
		// plane
		RenderingUtils.renderPlane(matrices, buffers, portal.plane, 2.5f, Color.ORANGE);
		// collision bounds
//		RenderingUtils.renderBox(matrices, buffers, portal.entityCollisionBounds, Color.RED);
		RenderingUtils.renderBox(matrices, buffers, portal.blockModificationArea, Color.PURPLE);
		portal.blockModificationShapes.forEach(($, shape) -> RenderingUtils.renderShape(matrices, buffers, shape, Color.CYAN));
		// cross-portal collision
//		renderCollision(ctx, portal, linked);
		// render player's raycast through
		Vec3 pos = camera.getPosition();
		Vector3f lookVector = camera.getLookVector().normalize(3, new Vector3f());
		Vec3 end = pos.add(lookVector.x, lookVector.y, lookVector.z);
		PortalHitResult hit = ctx.world().portalManager().activePortals().clip(pos, end);
		if (hit != null) {
			// start -> hitIn
			RenderingUtils.renderLine(matrices, buffers, hit.start(), hit.inHit(), Color.ORANGE);
			// box at hitIn
			AABB hitInBox = AABB.ofSize(hit.inHit(), 0.1, 0.1, 0.1);
			RenderingUtils.renderBox(matrices, buffers, hitInBox, Color.ORANGE);
			// box at hitOut
			AABB hitOutBox = AABB.ofSize(hit.outHit(), 0.1, 0.1, 0.1);
			RenderingUtils.renderBox(matrices, buffers, hitOutBox, Color.BLUE);

			if (hit.isEnd()) {
				// hitOut -> end
				RenderingUtils.renderLine(matrices, buffers, hit.outHit(), hit.end(), Color.BLUE);
				// box at end
				AABB endBox = AABB.ofSize(hit.end(), 0.1, 0.1, 0.1);
				RenderingUtils.renderBox(matrices, buffers, endBox, Color.BLUE);
			}
		}
		matrices.popPose();
	}

	private static void renderTransform(PortalInstance in, PortalInstance out, PoseStack matrices, MultiBufferSource buffers) {
		PortalTransform transform = new PortalTransform(in, out);
		for (TransformSample sample : getSamplePositions(in)) {
			RenderingUtils.renderPos(matrices, buffers, sample.pos, 0.1f, sample.color);
			Vec3 transformed = transform.applyAbsolute(sample.pos);
			RenderingUtils.renderPos(matrices, buffers, transformed, 0.1f, sample.color);
		}
	}

	private static TransformSample[] getSamplePositions(PortalInstance portal) {
		Vec3 out = portal.normal.scale(0.25);
		Vec3 up = portal.quad.up().scale(0.25);
		Vec3 right = portal.quad.right().scale(0.25);
		Vec3 base = portal.data.origin().add(out);
		return new TransformSample[] {
				new TransformSample(base.add(up).add(right), Color.GREEN),
				new TransformSample(base.add(up).subtract(right), Color.RED),
				new TransformSample(base.subtract(up).add(right), Color.YELLOW),
				new TransformSample(base.subtract(up).subtract(right), Color.BLUE)
		};
	}

	private record TransformSample(Vec3 pos, Color color) {
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
