package io.github.fusionflux.portalcubed.framework.render.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.PortalCubedClient;
import io.github.fusionflux.portalcubed.framework.render.debug.entry.DebugBox;
import io.github.fusionflux.portalcubed.framework.render.debug.entry.DebugLine;
import io.github.fusionflux.portalcubed.framework.render.debug.entry.DebugPos;
import io.github.fusionflux.portalcubed.framework.shape.Line;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.framework.util.Color;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DebugRendering {
	private static final List<DebugRenderEntry> entries = Collections.synchronizedList(new ArrayList<>());

	public static void addPos(int ticks, Vec3 pos, Color color) {
	    entries.add(new DebugPos(ticks, nudge(pos), color));
	}

	public static void addLine(int ticks, Line line, Color color) {
		entries.add(new DebugLine(ticks, line, color));
	}

	public static void addBox(int ticks, AABB box, Color color) {
	    entries.add(new DebugBox(ticks, box, color));
	}

	public static void addBox(int ticks, OBB box, Color color) {
		entries.add(new DebugBox(ticks, box, color));
	}

	public static void addShape(int ticks, VoxelShape shape, Color color) {
		shape.toAabbs().forEach(box -> addBox(ticks, box, color));
	}

	// internal

	public static void init() {
		ClientTickEvents.START_CLIENT_TICK.register(DebugRendering::tick);
		WorldRenderEvents.AFTER_ENTITIES.register(DebugRendering::render);
	}

	private static void tick(Minecraft mc) {
		entries.removeIf(entry -> entry.ticksLeft-- <= 0);
	}

	private static Vec3 nudge(Vec3 pos) {
		double offset = Math.random() / 1000;
		return pos.add(offset);
	}

	private static void render(WorldRenderContext ctx) {
		if (!PortalCubedClient.portalDebugEnabled || entries.isEmpty())
			return;

		PoseStack matrices = ctx.matrixStack();
		MultiBufferSource vertices = Objects.requireNonNull(ctx.consumers());
		Camera camera = ctx.camera();

		matrices.pushPose();
		matrices.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
		for (DebugRenderEntry entry : new ArrayList<>(entries)) {
			matrices.pushPose();
			entry.render(matrices, vertices);
			matrices.popPose();
		}
		matrices.popPose();
	}
}
