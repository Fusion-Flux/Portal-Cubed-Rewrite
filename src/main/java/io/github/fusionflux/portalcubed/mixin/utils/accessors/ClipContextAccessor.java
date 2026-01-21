package io.github.fusionflux.portalcubed.mixin.utils.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.CollisionContext;

@Mixin(ClipContext.class)
public interface ClipContextAccessor {
	@Accessor
	ClipContext.Block getBlock();

	@Accessor
	ClipContext.Fluid getFluid();

	@Accessor
	CollisionContext getCollisionContext();
}
