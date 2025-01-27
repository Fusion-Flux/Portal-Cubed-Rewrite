package io.github.fusionflux.portalcubed.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;
import io.github.fusionflux.portalcubed.framework.extension.LevelExt;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;

@Mixin(Level.class)
public class LevelMixin implements LevelExt {
	@Shadow
	@Final
	private RegistryAccess registryAccess;

	@Unique
	private PortalCubedDamageSources damageSources;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		this.damageSources = new PortalCubedDamageSources(this.registryAccess);
	}

	@Override
	public PortalCubedDamageSources pc$damageSources() {
		return this.damageSources;
	}
}
