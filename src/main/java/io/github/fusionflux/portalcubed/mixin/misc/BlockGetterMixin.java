package io.github.fusionflux.portalcubed.mixin.misc;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.framework.extension.BigShapeBlock;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(BlockGetter.class)
public interface BlockGetterMixin {
	@Shadow
	BlockState getBlockState(BlockPos pos);

	@WrapOperation(
			method = "clip",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/BlockGetter;traverseBlocks(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Ljava/lang/Object;Ljava/util/function/BiFunction;Ljava/util/function/Function;)Ljava/lang/Object;"
			)
	)
	private <T, C> T checkBigBlocks(Vec3 from, Vec3 to, C context, BiFunction<C, BlockPos, T> originalTester, Function<C, T> onFail, Operation<T> original) {
		LongSet bigBlocks = new LongOpenHashSet();
		BiFunction<C, BlockPos, T> testerWrapper = this.wrapTester(originalTester, bigBlocks);
		T result = original.call(from, to, context, testerWrapper, onFail);

		if (bigBlocks.isEmpty())
			return result;

		BlockHitResult closestHit = (BlockHitResult) result;
		double closestDistSqr = closestHit.getLocation().distanceToSqr(from);

		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (LongIterator itr = bigBlocks.iterator(); itr.hasNext();) {
			cursor.set(itr.nextLong());
			BlockHitResult bigBlockResult = (BlockHitResult) originalTester.apply(context, cursor);
			if (bigBlockResult != null && bigBlockResult.getType() != HitResult.Type.MISS) {
				double distanceSqr = bigBlockResult.getLocation().distanceToSqr(from);
				if (distanceSqr < closestDistSqr) {
					closestHit = bigBlockResult;
					closestDistSqr = distanceSqr;
				}
			}
		}

		//noinspection unchecked
		return (T) closestHit;
	}

	@Unique
	private <T, C> BiFunction<C, BlockPos, T> wrapTester(BiFunction<C, BlockPos, T> original, LongSet set) {
		return (context, pos) -> {
			Iterable<BlockPos> adjacentBlocks = BlockPos.betweenClosed(
					pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
					pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
			);

			for (BlockPos adjacent : adjacentBlocks) {
				if (adjacent.equals(pos))
					continue;

				BlockState state = this.getBlockState(adjacent);
				if (state.getBlock() instanceof BigShapeBlock) {
					set.add(adjacent.asLong());
				}
			}

			return original.apply(context, pos);
		};
	}
}
