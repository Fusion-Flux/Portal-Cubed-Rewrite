package io.github.fusionflux.portalcubed.framework.render.debug.entry;

import io.github.fusionflux.portalcubed.framework.render.debug.DebugRenderEntry;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DebugPos extends DebugRenderEntry {
	public static final double BOX_SIZE = 0.2;
	private final AABB box;
	private final Color color;

	public DebugPos(int ticks, Vec3 pos, Color color) {
		super(ticks);
		this.box = AABB.ofSize(pos, BOX_SIZE, BOX_SIZE, BOX_SIZE);
		this.color = color;
	}

	@Override
	public void render(PoseStack matrices, MultiBufferSource vertices) {
		RenderingUtils.renderBox(matrices, vertices, this.box, this.color);
	}
}
