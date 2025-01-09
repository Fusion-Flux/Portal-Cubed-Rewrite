package io.github.fusionflux.portalcubed.framework.render.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.framework.render.debug.entry.DebugPos;
import io.github.fusionflux.portalcubed.framework.util.Color;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

public class DebugRendering {
	private static final List<DebugRenderEntry> entries = new ArrayList<>();

	public static void addPos(int ticks, Vec3 pos, Color color) {
	    entries.add(new DebugPos(60, pos, color));
	}

	// internal

	public static void init() {
		ClientTickEvents.START.register(DebugRendering::tick);
		WorldRenderEvents.AFTER_ENTITIES.register(DebugRendering::render);
	}

	private static void tick(Minecraft mc) {
		entries.removeIf(entry -> entry.ticksLeft-- <= 0);
	}

	private static void render(WorldRenderContext ctx) {
		if (entries.isEmpty())
			return;

		PoseStack matrices = ctx.matrixStack();
		MultiBufferSource vertices = Objects.requireNonNull(ctx.consumers());
		Camera camera = ctx.camera();

		matrices.pushPose();
		matrices.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
		for (DebugRenderEntry entry : entries) {
			matrices.pushPose();
			entry.render(matrices, vertices);
			matrices.popPose();
		}
		matrices.popPose();
	}
}
