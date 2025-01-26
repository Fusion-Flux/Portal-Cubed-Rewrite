package io.github.fusionflux.portalcubed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.animal.Pufferfish;

@Mixin(Pufferfish.class)
public interface PufferfishAccessor {
	@Accessor
	void setDeflateTimer(int timer);
}
