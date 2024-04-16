package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.framework.block.TransparentSlabBlock;
import net.minecraft.world.level.block.SlabBlock;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.block.state.properties.Property;

import net.minecraft.world.level.block.state.properties.SlabType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SlabBlock.class)
public class SlabBlockMixin {
	@SuppressWarnings("rawtypes")
	@WrapOperation(method = {"placeLiquid", "canPlaceLiquid"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
	private Comparable allowDoublePlace(BlockState instance, Property property, Operation<Comparable> original) {
		if (instance.getBlock() instanceof TransparentSlabBlock && property.equals(SlabBlock.TYPE))
			return SlabType.BOTTOM;
		return original.call(instance, property);
	}
}
