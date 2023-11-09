package io.github.fusionflux.portalcubed.mixin;

import net.minecraft.world.phys.shapes.CubeVoxelShape;

import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CubeVoxelShape.class)
public interface CubeVoxelShapeAccessor {
	@Invoker("<init>")
	static CubeVoxelShape pc$create(DiscreteVoxelShape shape) {
		throw new AbstractMethodError();
	}
}
