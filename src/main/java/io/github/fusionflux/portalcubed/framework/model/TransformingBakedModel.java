package io.github.fusionflux.portalcubed.framework.model;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class TransformingBakedModel extends ForwardingBakedModel {
	private final RenderContext.QuadTransform transform;

	public TransformingBakedModel(RenderContext.QuadTransform transform) {
		this.transform = transform;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		context.pushTransform(this.transform);
		super.emitItemQuads(stack, randomSupplier, context);
		context.popTransform();
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		context.pushTransform(this.transform);
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();
	}
}
