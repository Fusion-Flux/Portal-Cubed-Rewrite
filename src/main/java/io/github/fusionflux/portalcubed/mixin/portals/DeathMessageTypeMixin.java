package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.world.damagesource.DeathMessageType;

@Mixin(DeathMessageType.class)
public enum DeathMessageTypeMixin {
	PORTALCUBED_MIRROR_TEST(PortalCubed.idString("mirror_test"));

	@Shadow
	DeathMessageTypeMixin(String id) {}
}
