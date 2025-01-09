package io.github.fusionflux.portalcubed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.door.ChamberDoorBlock;
import io.github.fusionflux.portalcubed.content.door.LockingChamberDoorBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

@Mixin(DoorBlock.class)
public class DoorBlockMixin {
	@SuppressWarnings("rawtypes")
	@WrapOperation(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;",
					ordinal = 1
			)
	)
	private Object registerStatePropertyDefault(BlockState instance, Property property, Comparable comparable, Operation<Object> original) {
		if ((Object) this instanceof LockingChamberDoorBlock)
			return original.call(instance, LockingChamberDoorBlock.STATE, LockingChamberDoorBlock.State.CLOSED);
		return original.call(instance, property, comparable);
	}

	@SuppressWarnings("rawtypes")
	@WrapOperation(
			method = "getStateForPlacement",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;",
					ordinal = 3
			)
	)
	private Object setStatePropertyForPlacement(BlockState instance, Property property, Comparable comparable, Operation<Object> original) {
		if ((Object) this instanceof LockingChamberDoorBlock)
			return original.call(instance, LockingChamberDoorBlock.STATE, ((Boolean) comparable) ? LockingChamberDoorBlock.State.OPEN : LockingChamberDoorBlock.State.CLOSED);
		return original.call(instance, property, comparable);
	}

	@SuppressWarnings("rawtypes")
	@WrapOperation(
			method = "isPathfindable",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"
			)
	)
	private Comparable useIsOpen(BlockState instance, Property property, Operation<Comparable> original) {
		return ((DoorBlock) (Object) this).isOpen(instance);
	}

	@ModifyArg(
			method = "playSound",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
			),
			index = 5
	)
	private float noRandomPitch(float pitch) {
		return ((Object) this instanceof ChamberDoorBlock) ? 1f : pitch;
	}
}
