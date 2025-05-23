package io.github.fusionflux.portalcubed.mixin.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.framework.entity.EntityTickWrapper;
import io.github.fusionflux.portalcubed.framework.extension.ClientLevelExt;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin implements ClientLevelExt {
	@Unique
	private ClientPortalManager portalManager;

	@Shadow
	@Final
	@Mutable
	private static Set<Item> MARKER_PARTICLE_ITEMS;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void givePortalCubedBarrierMarkerParticles(CallbackInfo ci) {
		Set<Item> newMarkerParticleItems = new HashSet<>(MARKER_PARTICLE_ITEMS);
		newMarkerParticleItems.add(PortalCubedBlocks.PROP_BARRIER.asItem());
		newMarkerParticleItems.add(PortalCubedBlocks.PORTAL_BARRIER.asItem());
		MARKER_PARTICLE_ITEMS = Collections.unmodifiableSet(newMarkerParticleItems);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		this.portalManager = new ClientPortalManager((ClientLevel) (Object) this);
	}

	@WrapOperation(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
	private void disintegrationTick(Entity instance, Operation<Void> original) {
		EntityTickWrapper.handle(instance, original);
	}

	@Override
	public ClientPortalManager portalManager() {
		return this.portalManager;
	}
}
