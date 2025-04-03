package io.github.fusionflux.portalcubed.framework.render.debug.entry;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.framework.render.debug.DebugRenderEntry;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

public final class DebugBox extends DebugRenderEntry {
	private final AABB box;
	private final Color color;

	public DebugBox(int ticks, AABB box, Color color) {
		super(ticks);
		this.box = box;
		this.color = color;
	}

	@Override
	public void render(PoseStack matrices, MultiBufferSource vertices) {
		RenderingUtils.renderBox(matrices, vertices, this.box, this.color);
	}
}
