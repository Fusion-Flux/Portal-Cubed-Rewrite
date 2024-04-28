package io.github.fusionflux.portalcubed.mixin.client;

import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.framework.extension.ClientLevelExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;


import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements ClientLevelExt {
	@Unique
	private ClientPortalManager portalManager;

	@Shadow
	@Final
	@Mutable
	private static Set<Item> MARKER_PARTICLE_ITEMS;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void givePropBarrierMarkerParticles(CallbackInfo ci) {
		Set<Item> newMarkerParticleItems = new HashSet<>(MARKER_PARTICLE_ITEMS);
		newMarkerParticleItems.add(PortalCubedBlocks.PROP_BARRIER.asItem());
		MARKER_PARTICLE_ITEMS = Collections.unmodifiableSet(newMarkerParticleItems);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		this.portalManager = new ClientPortalManager((ClientLevel) (Object) this);
	}

	@Override
	public ClientPortalManager pc$portalManager() {
		return this.portalManager;
	}

	// Overrides pc$playSoundInstance from LevelExt
	public void pc$playSoundInstance(Object soundInstance) {
		Minecraft.getInstance().getSoundManager().play((SoundInstance) soundInstance);
	}
}
