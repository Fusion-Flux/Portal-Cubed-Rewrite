package io.github.fusionflux.portalcubed.mixin.goo;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.material.FogType;

@Mixin(FogType.class)
public enum FogTypeMixin {
	PORTALCUBED_GOO
}
