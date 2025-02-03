package io.github.fusionflux.portalcubed.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.framework.block.TransparentSlabBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;

@Mixin(SlabBlock.class)
public class SlabBlockMixin {
	@WrapOperation(
			method = {"placeLiquid", "canPlaceLiquid"},
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"
			)
	)
	private Comparable<SlabType> allowDoublePlace(BlockState instance, Property<SlabType> property, Operation<Comparable<SlabType>> original) {
		if (instance.getBlock() instanceof TransparentSlabBlock && property.equals(SlabBlock.TYPE))
			return SlabType.BOTTOM;
		return original.call(instance, property);
	}
}
