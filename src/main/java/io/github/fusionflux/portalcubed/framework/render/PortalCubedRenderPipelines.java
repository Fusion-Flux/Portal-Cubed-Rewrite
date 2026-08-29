package io.github.fusionflux.portalcubed.framework.render;

import static net.minecraft.client.renderer.RenderPipelines.PARTICLE_SNIPPET;
import static net.minecraft.client.renderer.RenderPipelines.register;

import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import io.github.fusionflux.portalcubed.PortalCubed;

@SuppressWarnings("deprecation")
public interface PortalCubedRenderPipelines {
	RenderPipeline MULTIPLY_PARTICLE = register(
			RenderPipeline.builder(PARTICLE_SNIPPET)
					.withLocation(PortalCubed.id("pipeline/multiply_particle"))
					.withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.ZERO, BlendFactor.SRC_COLOR)))
					.build()
	);
}
