package io.github.fusionflux.portalcubed.framework.model.dynamictexture;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.util.ModelUtil;
import io.github.fusionflux.portalcubed.framework.util.WrapperQuadEmitter;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class DynamicTextureBakedModel extends WrapperBlockStateModel {
	private static final ThreadLocal<TextureReplacer> TEXTURE_REPLACER = ThreadLocal.withInitial(TextureReplacer::new);

	public DynamicTextureBakedModel(BlockStateModel wrapped) {
		super(wrapped);
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
		if (level.getBlockEntityRenderData(pos) instanceof DynamicTextureRenderData(Map<String, Identifier> map)) {
			TextureReplacer textureReplacer = TEXTURE_REPLACER.get();
			textureReplacer.prepare(map::get, emitter);
			super.emitQuads(textureReplacer, level, pos, state, random, cullTest);
			textureReplacer.cleanup();
		} else {
			super.emitQuads(emitter, level, pos, state, random, cullTest);
		}
	}

	private static final class TextureReplacer extends WrapperQuadEmitter {
		private Function<String, Identifier> mapper;

		private void prepare(Function<String, Identifier> mapper, QuadEmitter wrapped) {
			this.wrapped = wrapped;
			this.mapper = mapper;
		}

		private void cleanup() {
			this.wrapped = null;
			this.mapper = null;
		}

		@Override
		public QuadEmitter fromBakedQuad(BakedQuad quad) {
			super.fromBakedQuad(quad);

			BakedQuad.MaterialInfo materialInfo = quad.materialInfo();
			Optional.ofNullable(materialInfo.pc$textureReference())
					.map(this.mapper)
					.ifPresent(replacementTexture -> {
						ModelUtil.normalizeUV(this, quad.materialInfo().sprite());
						Material.Baked replacementMaterial = new Material.Baked(ModelUtil.getSprite(replacementTexture), false);
						this.materialBake(replacementMaterial, MutableQuadView.BAKE_NORMALIZED);
					});

			return this;
		}
	}
}
