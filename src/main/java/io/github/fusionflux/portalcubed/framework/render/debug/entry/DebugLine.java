package io.github.fusionflux.portalcubed.framework.render.debug.entry;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.framework.render.debug.DebugRenderEntry;
import io.github.fusionflux.portalcubed.framework.shape.Line;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import net.minecraft.client.renderer.MultiBufferSource;

public class DebugLine extends DebugRenderEntry {
	private final Line line;
	private final Color color;

	public DebugLine(int ticks, Line line, Color color) {
		super(ticks);
		this.line = line;
		this.color = color;
	}

	@Override
	public void render(PoseStack matrices, MultiBufferSource vertices) {
		RenderingUtils.renderLine(matrices, vertices, this.line, this.color);
	}
}
