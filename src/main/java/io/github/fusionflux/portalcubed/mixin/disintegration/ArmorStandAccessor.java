package io.github.fusionflux.portalcubed.mixin.disintegration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.ArmorStand;

@Mixin(ArmorStand.class)
public interface ArmorStandAccessor {
	@Invoker
	void callBrokenByAnything(ServerLevel level, DamageSource damageSource);
}
