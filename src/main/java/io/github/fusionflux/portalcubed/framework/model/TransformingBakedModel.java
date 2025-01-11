package io.github.fusionflux.portalcubed.framework.model;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.model.loading.v1.UnwrappableBakedModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadTransform;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class TransformingBakedModel implements BakedModel, UnwrappableBakedModel {
	protected BakedModel delegate;
	private final QuadTransform transform;

	public TransformingBakedModel(QuadTransform transform) {
		this.transform = transform;
	}

	@Override
	@Nullable
	public BakedModel getWrappedModel() {
		return this.delegate;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest) {
		emitter.pushTransform(this.transform);
		this.delegate.emitBlockQuads(emitter, blockView, state, pos, randomSupplier, cullTest);
		emitter.popTransform();
	}

	@Override
	public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier) {
		emitter.pushTransform(this.transform);
		this.delegate.emitItemQuads(emitter, randomSupplier);
		emitter.popTransform();
	}

	@NotNull
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
		return this.delegate.getQuads(state, direction, random);
	}

	public boolean useAmbientOcclusion() {
		return this.delegate.useAmbientOcclusion();
	}

	public boolean isGui3d() {
		return this.delegate.isGui3d();
	}

	public boolean usesBlockLight() {
		return this.delegate.usesBlockLight();
	}

	@NotNull
	public TextureAtlasSprite getParticleIcon() {
		return this.delegate.getParticleIcon();
	}

	@NotNull
	public ItemTransforms getTransforms() {
		return this.delegate.getTransforms();
	}
}
