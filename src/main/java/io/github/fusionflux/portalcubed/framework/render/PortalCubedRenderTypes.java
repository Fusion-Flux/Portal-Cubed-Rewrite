package io.github.fusionflux.portalcubed.framework.render;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;

@SuppressWarnings("deprecation")
public interface PortalCubedRenderTypes {
	BiFunction<RenderStateShard.DepthTestStateShard, ResourceLocation, RenderType> DEPTH_CUTOUT = Util.memoize((depthTest, texture) -> RenderType.create(
			"portalcubed:depth_cutout",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			RenderType.TRANSIENT_BUFFER_SIZE,
			false,
			false,
			RenderType.CompositeState.builder()
					.setShaderState(RenderStateShard.POSITION_TEX_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
					.setWriteMaskState(RenderStateShard.DEPTH_WRITE)
					.setDepthTestState(depthTest)
					.createCompositeState(false)
	));

	Function<ResourceLocation, RenderType> EMISSIVE = Util.memoize(texture -> RenderType.create(
			"portalcubed:emissive",
			DefaultVertexFormat.BLOCK,
			VertexFormat.Mode.QUADS,
			RenderType.TRANSIENT_BUFFER_SIZE,
			false,
			true,
			RenderType.CompositeState.builder()
					.setShaderState(RenderStateShard.RENDERTYPE_CUTOUT_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
					.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
					.setLightmapState(RenderStateShard.LIGHTMAP)
					.createCompositeState(false)
	));

	RenderType MULTIPLY_PARTICLE = RenderType.create(
			"portalcubed:multiply_particle",
			DefaultVertexFormat.PARTICLE,
			VertexFormat.Mode.QUADS,
			RenderType.TRANSIENT_BUFFER_SIZE,
			false,
			false,
			RenderType.CompositeState.builder()
					.setShaderState(RenderStateShard.PARTICLE_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_PARTICLES, TriState.FALSE, false))
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
	);

	static RenderType depthCutout(RenderStateShard.DepthTestStateShard depthTest, ResourceLocation texture) {
		return DEPTH_CUTOUT.apply(depthTest, texture);
	}

	static RenderType emissive(ResourceLocation texture) {
		return EMISSIVE.apply(texture);
	}
}
