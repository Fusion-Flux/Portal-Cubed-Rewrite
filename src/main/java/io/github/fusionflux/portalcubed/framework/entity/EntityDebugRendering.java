package io.github.fusionflux.portalcubed.framework.entity;

import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.content.portal.sync.TeleportProgressTracker;
import io.github.fusionflux.portalcubed.content.portal.sync.TrackedTeleport;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class
EntityDebugRendering {
	public static void init() {
		WorldRenderEvents.AFTER_ENTITIES.register(EntityDebugRendering::render);
	}

	private static void render(WorldRenderContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.getEntityRenderDispatcher().shouldRenderHitBoxes() || mc.showOnlyReducedInfo())
			return;

		PoseStack matrices = ctx.matrixStack();
		MultiBufferSource vertices = Objects.requireNonNull(ctx.consumers());
		Camera camera = ctx.camera();
		ClientLevel level = ctx.world();

		matrices.pushPose();
		matrices.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

		for (Entity entity : level.entitiesForRendering()) {
			if (shouldRender(entity, mc)) {
				render(entity, matrices, vertices, Color.BLUE, Color.PURPLE);
				Entity serverEntity = getServerEntity(entity, mc);
				if (serverEntity != null) {
					render(serverEntity, matrices, vertices, Color.GREEN, Color.CYAN);
					RenderingUtils.renderVec(matrices, vertices, serverEntity.getViewVector(1), serverEntity.getEyePosition(1), Color.RED);
				}

				TeleportProgressTracker tracker = entity.getTeleportProgressTracker();
				TrackedTeleport teleport = tracker.currentTeleport();
				if (teleport != null) {
					RenderingUtils.renderPlane(matrices, vertices, teleport.threshold, 3f, Color.CYAN);
				}
			}
		}

		matrices.popPose();
	}

	private static void render(Entity entity, PoseStack matrices, MultiBufferSource vertices, Color bounds, Color vel) {
		RenderingUtils.renderBox(matrices, vertices, entity.getBoundingBox(), bounds);
		RenderingUtils.renderVec(matrices, vertices, entity.getDeltaMovement(), Vec3.ZERO, vel);
	}

	private static boolean shouldRender(Entity entity, Minecraft mc) {
		if (entity.isInvisible())
			return false;

		if (entity == mc.player) {
			return mc.options.getCameraType() != CameraType.FIRST_PERSON;
		}

		return true;
	}

	private static Entity getServerEntity(Entity entity, Minecraft mc) {
		IntegratedServer server = mc.getSingleplayerServer();
		if (server != null) {
			ServerLevel level = server.getLevel(entity.level().dimension());
			if (level != null) {
				return level.getEntity(entity.getId());
			}
		}
		return null;
	}
}
