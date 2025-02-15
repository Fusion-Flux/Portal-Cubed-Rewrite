package io.github.fusionflux.portalcubed.mixin.utils.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.vehicle.OldMinecartBehavior;

@Mixin(OldMinecartBehavior.class)
public interface OldMinecartBehaviorAccessor {
	@Accessor
	int getLerpSteps();

	@Accessor
	void setLerpSteps(int steps);
}
