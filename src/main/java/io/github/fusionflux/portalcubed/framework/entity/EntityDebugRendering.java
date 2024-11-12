package io.github.fusionflux.portalcubed.framework.entity;

import java.util.Objects;

import io.github.fusionflux.portalcubed.content.portal.TeleportStep;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.RangeSequence;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityDebugRendering {
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
		float tickDelta = ctx.tickDelta();
		TickRateManager tickManager = level.tickRateManager();
		float realTickDelta = tickManager.runsNormally() ? tickDelta : 1.0F;

		matrices.pushPose();
		matrices.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

		for (Entity entity : level.entitiesForRendering()) {
			if (!entity.isInvisible() && entity != mc.player) {
				render(entity, matrices, vertices, Color.BLUE, Color.PURPLE);
				Entity serverEntity = getServerEntity(entity, mc);
				if (serverEntity != null) {
					render(serverEntity, matrices, vertices, Color.GREEN, Color.CYAN);
				}

				RangeSequence<TeleportStep> steps = entity.getPortalTeleport();
				if (steps != null) {

					for (RangeSequence.Entry<TeleportStep> entry : steps) {
						TeleportStep step = entry.value();
						RenderingUtils.renderBoxAround(matrices, vertices, step.from(), 0.1, Color.RED);
						RenderingUtils.renderBoxAround(matrices, vertices, step.to(), 0.1, Color.RED);
						RenderingUtils.renderLine(matrices, vertices, step.from(), step.to(), Color.RED);
					}
					float tickDeltaToUse = tickManager.isEntityFrozen(entity) ? realTickDelta : tickDelta;
					RangeSequence.Entry<TeleportStep> entry = steps.getEntry(tickDeltaToUse);
					float range = entry.max() - entry.min();
					float localProgress = (tickDeltaToUse - entry.min()) / range;
					TeleportStep step = entry.value();
					Vec3 pos = step.pos(localProgress);
					AABB box = AABB.ofSize(pos, 0.2, 0.2, 0.2);
					RenderingUtils.renderBox(matrices, vertices, box, Color.PURPLE);
				}
			}
		}

		matrices.popPose();
	}

	private static void render(Entity entity, PoseStack matrices, MultiBufferSource vertices, Color bounds, Color vel) {
		RenderingUtils.renderBox(matrices, vertices, entity.getBoundingBox(), bounds);
		RenderingUtils.renderVec(matrices, vertices, entity.getDeltaMovement(), Vec3.ZERO, vel);
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
