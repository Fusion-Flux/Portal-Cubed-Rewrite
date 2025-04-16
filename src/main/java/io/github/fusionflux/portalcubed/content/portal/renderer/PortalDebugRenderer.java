package io.github.fusionflux.portalcubed.content.portal.renderer;

import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.PortalCubedClient;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
		RenderingUtils.renderVec(matrices, buffers, TransformUtils.toMc(portal.quad.up()), portal.data.origin(), Color.BLUE);
		// plane
		RenderingUtils.renderPlane(matrices, buffers, portal.plane, 2.5f, Color.ORANGE);
		// collision bounds
//		RenderingUtils.renderBox(matrices, buffers, portal.entityCollisionBounds, Color.RED);
		RenderingUtils.renderBox(matrices, buffers, portal.blockModificationArea, Color.PURPLE);
		portal.blockModificationShapes.forEach(($, shape) -> RenderingUtils.renderShape(matrices, buffers, shape, Color.CYAN));
		// cross-portal collision
//		renderCollision(ctx, portal, linked);
		// render player's raycast through
		LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
		Vec3 pos = player.getEyePosition();
		Vec3 lookVector = player.getViewVector(1).scale(6);
		Vec3 end = pos.add(lookVector);
		PortalHitResult hit = ctx.world().portalManager().activePortals().clip(pos, end);
		if (hit != null) {
			renderHit(matrices, buffers, hit);
		}
		matrices.popPose();
	}

	private static void renderHit(PoseStack matrices, MultiBufferSource vertices, PortalHitResult hit) {
		// in
		RenderingUtils.renderLine(matrices, vertices, hit.start(), hit.inHit(), Color.ORANGE);
		RenderingUtils.renderPos(matrices, vertices, hit.inHit(), 0.1f, Color.ORANGE);
		// intermediates, outs to next ins
		while (hit.hasNext()) {
			PortalHitResult next = hit.next();
			RenderingUtils.renderLine(matrices, vertices, hit.outHit(), next.inHit(), Color.CYAN);
			hit = next;
		}
		// hit is last, render out
		RenderingUtils.renderLine(matrices, vertices, hit.outHit(), hit.end(), Color.BLUE);
		RenderingUtils.renderPos(matrices, vertices, hit.end(), 0.1f, Color.BLUE);
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
