package io.github.fusionflux.portalcubed.framework.render.debug;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;

public abstract class DebugRenderEntry {
	int ticksLeft;

	public DebugRenderEntry(int ticks) {
		this.ticksLeft = ticks;
	}

	public abstract void render(PoseStack matrices, MultiBufferSource vertices);
}
