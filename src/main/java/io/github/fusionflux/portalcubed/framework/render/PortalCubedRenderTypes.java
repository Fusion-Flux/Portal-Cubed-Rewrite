package io.github.fusionflux.portalcubed.framework.render;

@SuppressWarnings("deprecation")
public interface PortalCubedRenderTypes {
	// TODO: These all need to be redone with portal rendering - Max
//	BiFunction<RenderStateShard.DepthTestStateShard, Identifier, RenderType> PORTAL_STENCIL = Util.memoize((depthTest, texture) -> RenderType.create(
//			"portalcubed:portal_stencil",
//			DefaultVertexFormat.POSITION_TEX,
//			VertexFormat.Mode.QUADS,
//			RenderType.TRANSIENT_BUFFER_SIZE,
//			false,
//			false,
//			RenderType.CompositeState.builder()
//					.setShaderState(RenderStateShard.POSITION_TEX_SHADER)
//					.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
//					.setWriteMaskState(RenderStateShard.DEPTH_WRITE)
//					.setDepthTestState(depthTest)
//					.createCompositeState(false)
//	));
//
//	Function<Identifier, RenderType> EMISSIVE = Util.memoize(texture -> RenderType.create(
//			"portalcubed:emissive",
//			DefaultVertexFormat.BLOCK,
//			VertexFormat.Mode.QUADS,
//			RenderType.TRANSIENT_BUFFER_SIZE,
//			false,
//			true,
//			RenderType.CompositeState.builder()
//					.setShaderState(RenderStateShard.RENDERTYPE_CUTOUT_SHADER)
//					.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
//					.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
//					.setLightmapState(RenderStateShard.LIGHTMAP)
//					.createCompositeState(false)
//	));
//
//	Function<Identifier, RenderType> TRACER = Util.memoize(texture -> RenderType.create(
//			"portalcubed:tracer",
//			DefaultVertexFormat.BLOCK,
//			VertexFormat.Mode.QUADS,
//			RenderType.TRANSIENT_BUFFER_SIZE,
//			false,
//			true,
//			RenderType.CompositeState.builder()
//					.setShaderState(RenderStateShard.RENDERTYPE_CUTOUT_SHADER)
//					.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
//					.setWriteMaskState(RenderStateShard.COLOR_WRITE)
//					.setDepthTestState(RenderStateShard.GREATER_DEPTH_TEST)
//					.setTransparencyState(
//							new RenderStateShard.TransparencyStateShard(
//									"constant_transparency",
//									() -> {
//										RenderSystem.enableBlend();
//										RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
//									},
//									() -> {
//										RenderSystem.disableBlend();
//										RenderSystem.defaultBlendFunc();
//									}
//							)
//					)
//					.setCullState(RenderStateShard.NO_CULL)
//					.setLightmapState(RenderStateShard.LIGHTMAP)
//					.createCompositeState(false)
//	));
//
//	RenderType MULTIPLY_PARTICLE = RenderType.create(
//			"portalcubed:multiply_particle",
//			DefaultVertexFormat.PARTICLE,
//			VertexFormat.Mode.QUADS,
//			RenderType.TRANSIENT_BUFFER_SIZE,
//			false,
//			false,
//			RenderType.CompositeState.builder()
//					.setShaderState(RenderStateShard.PARTICLE_SHADER)
//					.setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_PARTICLES, TriState.FALSE, false))
//					.setTransparencyState(
//							new RenderStateShard.TransparencyStateShard(
//									"multiply_transparency",
//									() -> {
//										RenderSystem.enableBlend();
//										RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.SRC_COLOR);
//									},
//									() -> {
//										RenderSystem.disableBlend();
//										RenderSystem.defaultBlendFunc();
//									}
//							)
//					)
//					.setOutputState(RenderStateShard.PARTICLES_TARGET)
//					.setLightmapState(RenderStateShard.LIGHTMAP)
//					.setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
//					.createCompositeState(false)
//	);
//
//	static RenderType portalStencil(RenderStateShard.DepthTestStateShard depthTest, Identifier texture) {
//		return PORTAL_STENCIL.apply(depthTest, texture);
//	}
//
//	static RenderType emissive(Identifier texture) {
//		return EMISSIVE.apply(texture);
//	}
//
//	static RenderType tracer(Identifier texture) {
//		return TRACER.apply(texture);
//	}
}
