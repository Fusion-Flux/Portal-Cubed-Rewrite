package io.github.fusionflux.portalcubed.mixin;

import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
	@Invoker
	int callGetCurrentSwingDuration();

	@Accessor
	int getLerpSteps();

	@Accessor
	void setLerpSteps(int steps);
}
