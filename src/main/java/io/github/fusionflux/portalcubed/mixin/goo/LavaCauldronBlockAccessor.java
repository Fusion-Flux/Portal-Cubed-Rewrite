package io.github.fusionflux.portalcubed.mixin.goo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.LavaCauldronBlock;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(LavaCauldronBlock.class)
public interface LavaCauldronBlockAccessor {
	@Accessor
	static VoxelShape getFILLED_SHAPE() {
		throw new AbstractMethodError();
	}
}
