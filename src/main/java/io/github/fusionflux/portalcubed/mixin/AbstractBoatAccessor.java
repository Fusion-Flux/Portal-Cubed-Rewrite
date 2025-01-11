package io.github.fusionflux.portalcubed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.vehicle.AbstractBoat;

@Mixin(AbstractBoat.class)
public interface AbstractBoatAccessor {
	@Accessor
	int getLerpSteps();

	@Accessor
	void setLerpSteps(int steps);
}
