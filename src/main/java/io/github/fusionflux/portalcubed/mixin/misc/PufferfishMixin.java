package io.github.fusionflux.portalcubed.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.github.fusionflux.portalcubed.content.button.ButtonActivated;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pufferfish;

@Mixin(Pufferfish.class)
public abstract class PufferfishMixin implements ButtonActivated {
	@Shadow
	int deflateTimer;

	@Shadow
	public abstract int getPuffState();

	@Shadow
	public abstract void setPuffState(int puffState);

	@Override
	public void pc$onButtonActivated() {
		if (this.getPuffState() != Pufferfish.STATE_FULL) {
			((LivingEntity) (Object) this).makeSound(SoundEvents.PUFFER_FISH_BLOW_UP);
		}

		this.setPuffState(Pufferfish.STATE_FULL);
		this.deflateTimer = 0;
	}
}
