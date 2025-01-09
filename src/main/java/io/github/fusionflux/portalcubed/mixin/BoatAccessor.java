package io.github.fusionflux.portalcubed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.vehicle.Boat;

@Mixin(Boat.class)
public interface BoatAccessor {
	@Accessor
	int getLerpSteps();

	@Accessor
	void setLerpSteps(int steps);
}
