package io.github.fusionflux.portalcubed.mixin.chamber_door;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.door.ChamberDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

@Mixin(WalkNodeEvaluator.class)
public class WalkNodeEvaluatorMixin {
	@WrapOperation(
			method = "getPathTypeFromState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;",
					ordinal = 0
			)
	)
	private static Comparable<Boolean> useIsOpen(BlockState instance, Property<Boolean> property, Operation<Comparable<Boolean>> original) {
		return instance.getBlock() instanceof ChamberDoorBlock door ? door.isOpen(instance) : original.call(instance, property);
	}
}
