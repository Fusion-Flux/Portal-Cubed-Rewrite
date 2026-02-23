package io.github.fusionflux.portalcubed.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.github.fusionflux.portalcubed.content.button.ButtonActivated;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.armadillo.Armadillo;

@Mixin(Armadillo.class)
public abstract class ArmadilloMixin implements ButtonActivated {
	@Shadow
	public abstract void rollUp();

	@Shadow
	public abstract boolean canStayRolledUp();

	@Override
	public void pc$onButtonActivated() {
		if (!this.canStayRolledUp()) {
			return;
		}

		((LivingEntity) (Object) this).getBrain().setMemoryWithExpiry(MemoryModuleType.DANGER_DETECTED_RECENTLY, true, Armadillo.SCARE_CHECK_INTERVAL);
		this.rollUp();
	}
}
