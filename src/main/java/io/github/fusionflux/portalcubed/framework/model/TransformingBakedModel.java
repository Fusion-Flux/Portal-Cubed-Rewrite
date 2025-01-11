package io.github.fusionflux.portalcubed.framework.model;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadTransform;
import net.minecraft.client.resources.model.DelegateBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class TransformingBakedModel extends DelegateBakedModel {
	private final QuadTransform transform;

	public TransformingBakedModel(QuadTransform transform) {
		this.transform = transform;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest) {
		emitter.pushTransform(this.transform);
		super.emitBlockQuads(emitter, blockView, state, pos, randomSupplier, cullTest);
		emitter.popTransform();
	}

	@Override
	public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier) {
		emitter.pushTransform(this.transform);
		super.emitItemQuads(emitter, randomSupplier);
		emitter.popTransform();
	}
}
