package io.github.fusionflux.portalcubed.mixin.utils.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.ClipContext;

@Mixin(ClipContext.Block.class)
public interface ClipContext$BlockAccessor {
	@Invoker("<init>")
	static ClipContext.Block pc$create(String name, int ordinal, ClipContext.ShapeGetter shapeGetter) {
		throw new AbstractMethodError();
	}
}
