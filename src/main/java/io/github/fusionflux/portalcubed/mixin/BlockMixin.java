package io.github.fusionflux.portalcubed.mixin;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(Block.class)
public abstract class BlockMixin {
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
			for (ItemStack stack : drops) {
				BlockPos originPos = multiBlock.getOriginPos(pos, state);
				Vec3 center = multiBlock.size.rotated(state.getValue(AbstractMultiBlock.FACE))
						.center(0, 0, 0)
						.add(originPos.getX(), originPos.getY(), originPos.getZ());

				double yOffset = EntityType.ITEM.getHeight() / 2;
				double x = center.x + Mth.nextDouble(world.random, -.25, .25);
				double y = center.y + Mth.nextDouble(world.random, -.25, .25) - yOffset;
				double z = center.z + Mth.nextDouble(world.random, -.25, .25);

				popResource(world, () -> new ItemEntity(world, x, y, z, stack), stack);
			}
		} else {
			original.call(drops, consumer);
		}
	}

	@WrapOperation(
			method = "pushEntitiesUp",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;teleportRelative(DDD)V"
			)
	)
	private static void onPushUp(Entity instance, double offsetX, double offsetY, double offsetZ, Operation<Void> original) {
		original.call(instance, offsetX, offsetY, offsetZ);
		// teleportRelative calls teleportTo which assumes it's non-local
		// this is local, so undo that
		instance.pc$setNextTeleportNonLocal(false);
	}
}
