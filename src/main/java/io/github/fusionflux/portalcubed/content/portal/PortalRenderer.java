package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.framework.shape.VoxelShenanigans;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;

import net.minecraft.client.renderer.culling.Frustum;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PortalRenderer {
	public static final Color RED = new Color(1, 0, 0, 1);
	public static final Color GREEN = new Color(0.5f, 1, 0.5f, 1);
	public static final Color BLUE = new Color(0, 0, 1, 1);
	public static final Color ORANGE = new Color(1, 0.5f, 0, 1);
	public static final Color PURPLE = new Color(0.5f, 0, 1, 1);
	public static final Color CYAN = new Color(0, 1, 1, 1);

	public static final Color PLANE_COLOR = new Color(1, 1, 1, 1);
	public static final Color ACTIVE_PLANE_COLOR = GREEN;

	public static final double OFFSET_FROM_WALL = 0.001;

	private static void render(WorldRenderContext context) {
		if (!(context.consumers() instanceof final MultiBufferSource.BufferSource vertexConsumers))
			return;
		ClientPortalManager manager = context.world().portalManager();
		Collection<PortalPair> pairs = manager.getAllPairs();
		if (pairs.isEmpty())
			return;

		PoseStack matrices = context.matrixStack();
		Vec3 camPos = context.camera().getPosition();
		Frustum frustum = Objects.requireNonNull(context.frustum());
		matrices.pushPose();
		boolean renderDebug = Minecraft.getInstance().getDebugOverlay().showDebugScreen();
		matrices.translate(-camPos.x, -camPos.y, -camPos.z);
		for (PortalPair pair : pairs) {
			for (PortalInstance portal : pair) {
				if (frustum.isVisible(portal.renderBounds)) {
					renderPortal(portal, matrices, vertexConsumers);
//					if (renderDebug) {
//						renderPortalDebug(portal, context, matrices, vertexConsumers);
//					}
				}
			}
		}

		matrices.popPose();
		vertexConsumers.endLastBatch();
	}

	private static void renderPortal(PortalInstance portal, PoseStack matrices, MultiBufferSource vertexConsumers) {
		RenderType renderType = RenderType.beaconBeam(portal.data.settings().shape().texture, true);
		VertexConsumer vertices = vertexConsumers.getBuffer(renderType);
		matrices.pushPose();
		// translate to portal pos
		matrices.translate(portal.data.origin().x, portal.data.origin().y, portal.data.origin().z);
		// apply rotations
		matrices.mulPose(portal.rotation); // rotate towards facing direction
		matrices.mulPose(Axis.ZP.rotationDegrees(180));
		// slight offset so origin is center of portal
		matrices.translate(-0.5f, -1, 0);
		// small offset away from the wall to not z-fight
		matrices.translate(0, 0, -OFFSET_FROM_WALL);
		// scale quad - 32x32 texture, half is used. scale the 1x1 to a 2x2.
		matrices.scale(2, 2, 2);
		RenderingUtils.renderQuad(matrices, vertices, LightTexture.FULL_BRIGHT, portal.data.settings().color());
		matrices.popPose();
	}

//	private static void renderPortalDebug(PortalInstance portal, WorldRenderContext ctx, PoseStack matrices, MultiBufferSource vertexConsumers) {
//		// render a box around the portal's plane
//		Color planeColor = portal.isActive() ? ACTIVE_PLANE_COLOR : PLANE_COLOR;
//		renderBox(matrices, vertexConsumers, portal.plane, planeColor);
//		// collision bounds
//		renderBox(matrices, vertexConsumers, portal.entityCollisionArea, RED);
//		renderBox(matrices, vertexConsumers, portal.collisionCollectionArea, PURPLE);
//		renderBox(matrices, vertexConsumers, portal.collisionModificationBox, CYAN);
//		// cross-portal collision
//		PortalInstance linked = portal.getLinked();
//		if (linked != null) {
//			renderCollision(ctx, portal, linked);
//		}
//		// render player's raycast through
//		Camera camera = ctx.camera();
//		Vec3 pos = camera.getPosition();
//		Vector3f lookVector = camera.getLookVector().normalize(3, new Vector3f());
//		Vec3 end = pos.add(lookVector.x, lookVector.y, lookVector.z);
//		PortalHitResult hit = ctx.world().portalManager().clipPortal(pos, end);
//		if (hit != null) {
//			// start -> hitIn
//			RenderingUtils.renderLine(matrices, vertexConsumers, hit.start(), hit.hitIn(), ORANGE);
//			// box at hitIn
//			AABB hitInBox = AABB.ofSize(hit.hitIn(), 0.1, 0.1, 0.1);
//			renderBox(matrices, vertexConsumers, hitInBox, ORANGE);
//			// box at hitOut
//			AABB hitOutBox = AABB.ofSize(hit.hitOut(), 0.1, 0.1, 0.1);
//			renderBox(matrices, vertexConsumers, hitOutBox, BLUE);
//			// hitOut -> end
//			RenderingUtils.renderLine(matrices, vertexConsumers, hit.hitOut(), hit.teleportedEnd(), BLUE);
//			// box at end
//			AABB endBox = AABB.ofSize(hit.teleportedEnd(), 0.1, 0.1, 0.1);
//			renderBox(matrices, vertexConsumers, endBox, GREEN);
//		}
//	}

	private static void renderCollision(WorldRenderContext ctx, PortalInstance portal, PortalInstance linked) {
		Camera camera = ctx.camera();
		Entity entity = camera.getEntity();
		ClientLevel level = ctx.world();
		PoseStack matrices = ctx.matrixStack();
		VertexConsumer vertices = Objects.requireNonNull(ctx.consumers()).getBuffer(RenderType.lines());

		List<VoxelShape> shapes = VoxelShenanigans.getShapesBehindPortal(level, entity, portal, linked);
		shapes.forEach(shape -> LevelRenderer.renderVoxelShape(
				matrices, vertices, shape,
				0, 0, 0,
				1, 1, 1, 1,
				true
		));
	}

	public static void init() {
		WorldRenderEvents.AFTER_ENTITIES.register(PortalRenderer::render);
	}
}
