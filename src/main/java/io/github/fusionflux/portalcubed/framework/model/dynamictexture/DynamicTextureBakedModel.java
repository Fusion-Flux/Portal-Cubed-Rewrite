package io.github.fusionflux.portalcubed.framework.model.dynamictexture;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.util.DelegatingQuadEmitter;
import io.github.fusionflux.portalcubed.framework.util.ModelUtil;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.DelegateBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class DynamicTextureBakedModel extends DelegateBakedModel {
	private static final TextureReplacer TEXTURE_REPLACER = new TextureReplacer();

	public DynamicTextureBakedModel(BakedModel parent) {
		super(parent);
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest) {
		if (blockView.getBlockEntityRenderData(pos) instanceof DynamicTextureRenderData(Map<String, ResourceLocation> map)) {
			TEXTURE_REPLACER.prepare(map::get, emitter);
			super.emitBlockQuads(TEXTURE_REPLACER, blockView, state, pos, randomSupplier, cullTest);
			TEXTURE_REPLACER.cleanup();
		} else {
			super.emitBlockQuads(emitter, blockView, state, pos, randomSupplier, cullTest);
		}
	}

	@Override
	public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier) {
		super.emitItemQuads(emitter, randomSupplier);
	}

	private static final class TextureReplacer extends DelegatingQuadEmitter {
		private Function<String, ResourceLocation> mapper;

		private void prepare(Function<String, ResourceLocation> mapper, QuadEmitter delegate) {
			this.delegate = delegate;
			this.mapper = mapper;
		}

		private void cleanup() {
			this.delegate = null;
			this.mapper = null;
		}

		@Override
		public QuadEmitter fromVanilla(BakedQuad quad, RenderMaterial material, @Nullable Direction cullFace) {
			super.fromVanilla(quad, material, cullFace);
			Optional.ofNullable(quad.pc$textureReference())
					.map(this.mapper)
					.ifPresent(replacementTexture -> {
						ModelUtil.normalizeUV(this, quad.getSprite());
						this.spriteBake(ModelUtil.getSprite(replacementTexture), MutableQuadView.BAKE_NORMALIZED);
					});
			return this;
		}
	}
}
