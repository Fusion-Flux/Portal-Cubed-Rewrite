package io.github.fusionflux.portalcubed.mixin.utils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;
import io.github.fusionflux.portalcubed.framework.extension.LevelExt;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;

@Mixin(Level.class)
public class LevelMixin implements LevelExt {
	@Unique
	private PortalCubedDamageSources damageSources;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void init(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates, CallbackInfo ci) {
		this.damageSources = new PortalCubedDamageSources(registryAccess);
	}

	@Override
	public PortalCubedDamageSources pc$damageSources() {
		return this.damageSources;
	}
}
