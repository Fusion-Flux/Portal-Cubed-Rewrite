package io.github.fusionflux.portalcubed.mixin.misc;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.framework.block.multiblock.AbstractMultiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(Block.class)
public class BlockMixin {
	@Shadow
	private static void popResource(Level level, Supplier<ItemEntity> itemEntitySupplier, ItemStack stack) {
	}

	@WrapOperation(method = "dropResources*", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
	private static void spawnMultiblockDropsAtCenter(
			List<ItemStack> drops,
			Consumer<ItemStack> consumer,
			Operation<Void> original,
			@Local(argsOnly = true) BlockState state,
			@Local(argsOnly = true) Level world,
			@Local(argsOnly = true) BlockPos pos
	) {
		if (state.getBlock() instanceof AbstractMultiBlock multiBlock) {
			BlockPos originPos = multiBlock.getOriginPos(pos, state);
			Vec3 center = multiBlock.size.rotated(state.getValue(AbstractMultiBlock.FACE))
					.center(0, 0, 0)
					.subtract(0, EntityType.ITEM.getHeight() / 2, 0)
					.add(originPos.getX(), originPos.getY(), originPos.getZ());

			for (ItemStack stack : drops) {
				double x = center.x + Mth.nextDouble(world.random, -.25, .25);
				double y = center.y + Mth.nextDouble(world.random, -.25, .25);
				double z = center.z + Mth.nextDouble(world.random, -.25, .25);

				popResource(world, () -> new ItemEntity(world, x, y, z, stack), stack);
			}
		} else {
			original.call(drops, consumer);
		}
	}
}
