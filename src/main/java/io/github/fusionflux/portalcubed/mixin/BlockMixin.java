package io.github.fusionflux.portalcubed.mixin;

import java.util.function.Supplier;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.framework.block.multiblock.AbstractMultiBlock;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(Block.class)
public class BlockMixin {
	@Shadow
	private static void popResource(Level world, Supplier<ItemEntity> itemEntitySupplier, ItemStack stack) {
		throw new AssertionError();
	}

	@Inject(method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
	private static void spawnMultiblockDropsAtCenter(Level world, BlockPos pos, ItemStack stack, CallbackInfo ci) {
		BlockState state = world.getBlockState(pos);
		if (world instanceof ServerLevel && state.getBlock() instanceof AbstractMultiBlock multiBlock) {
			BlockPos originPos = multiBlock.getOriginPos(pos, state);
			Vec3 center = multiBlock.size.rotated(state.getValue(AbstractMultiBlock.FACING))
				.center(0, 0, .5)
				.add(originPos.getX(), originPos.getY(), originPos.getZ());

			double yOffset = EntityType.ITEM.getHeight() / 2;
			double x = center.x + Mth.nextDouble(world.random, -.25, .25);
			double y = center.y + Mth.nextDouble(world.random, -.25, .25) - yOffset;
			double z = center.z + Mth.nextDouble(world.random, -.25, .25);

			popResource(world, () -> new ItemEntity(world, x, y, z, stack), stack);

			ci.cancel();
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
