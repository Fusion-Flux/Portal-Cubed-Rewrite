package io.github.fusionflux.portalcubed.framework.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;

public class PortalCubedParticleRenderTypes {
	// there is no replacement as of 1.21.4
	@SuppressWarnings("deprecation")
	private static final ResourceLocation atlas = TextureAtlas.LOCATION_PARTICLES;

	public static final ParticleRenderType MULTIPLY = new ParticleRenderType(
			PortalCubed.id("multiply").toString(),
			RenderType.create(
					PortalCubed.id("multiply_particle").toString(),
					DefaultVertexFormat.PARTICLE,
					VertexFormat.Mode.QUADS,
					RenderType.TRANSIENT_BUFFER_SIZE,
					false,
					false,
					RenderType.CompositeState.builder()
							.setShaderState(RenderStateShard.PARTICLE_SHADER)
							.setTextureState(new RenderStateShard.TextureStateShard(atlas, TriState.FALSE, false))
							.setTransparencyState(
									new RenderStateShard.TransparencyStateShard(
											"multiply_transparency",
											() -> {
												RenderSystem.enableBlend();
												RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.SRC_COLOR);
											},
											() -> {
												RenderSystem.disableBlend();
												RenderSystem.defaultBlendFunc();
											}
									)
							)
							.setOutputState(RenderStateShard.PARTICLES_TARGET)
							.setLightmapState(RenderStateShard.LIGHTMAP)
							.setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
							.createCompositeState(false)
			)
	);
}
