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
	/*
	We have to register the default state here instead of the constructor,
	because the one in DoorBlock will crash the game due to the open property not being in the block state definition
	 */
	@WrapOperation(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;",
					ordinal = 1
			)
	)
	private Object registerStatePropertyDefault(BlockState instance, Property<Boolean> property, Comparable<Boolean> comparable, Operation<Object> original) {
		if ((Object) this instanceof LockingChamberDoorBlock)
			return original.call(instance, LockingChamberDoorBlock.STATE, LockingChamberDoorBlock.State.CLOSED);
		return original.call(instance, property, comparable);
	}

	@WrapOperation(
			method = "getStateForPlacement",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;",
					ordinal = 3
			)
	)
	private Object setStatePropertyForPlacement(BlockState instance, Property<Boolean> property, Comparable<Boolean> comparable, Operation<Object> original) {
		if ((Object) this instanceof LockingChamberDoorBlock)
			return original.call(instance, LockingChamberDoorBlock.STATE, ((Boolean) comparable) ? LockingChamberDoorBlock.State.OPEN : LockingChamberDoorBlock.State.CLOSED);
		return original.call(instance, property, comparable);
	}

	@WrapOperation(
			method = "isPathfindable",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"
			)
	)
	private Comparable<Boolean> useIsOpen(BlockState instance, Property<Boolean> property, Operation<Comparable<Boolean>> original) {
		return ((Object) this instanceof ChamberDoorBlock) ? ((DoorBlock) (Object) this).isOpen(instance) : original.call(instance, property);
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
