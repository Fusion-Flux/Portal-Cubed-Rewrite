package io.github.fusionflux.portalcubed.mixin.client;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
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

	@Definition(id = "of", method = "Ljava/util/Set;of(Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/Set;")
	@Definition(id = "BARRIER", field = "Lnet/minecraft/world/item/Items;BARRIER:Lnet/minecraft/world/item/Item;")
	@Definition(id = "LIGHT", field = "Lnet/minecraft/world/item/Items;LIGHT:Lnet/minecraft/world/item/Item;")
	@Expression("of(BARRIER, LIGHT)")
	@ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
	private static Set<Item> givePortalCubedBarrierMarkerParticles(Set<Item> original) {
		Set<Item> newMarkerParticleItems = new HashSet<>(original);
		newMarkerParticleItems.add(PortalCubedBlocks.PROP_BARRIER.asItem());
		newMarkerParticleItems.add(PortalCubedBlocks.PORTAL_BARRIER.asItem());
		return Set.copyOf(newMarkerParticleItems);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		this.portalManager = new ClientPortalManager((ClientLevel) (Object) this);
	}

	@WrapOperation(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
	private void wrapEntityTick(Entity instance, Operation<Void> original) {
		EntityTickWrapper.handle(instance, original);
	}

	@Override
	public ClientPortalManager portalManager() {
		return this.portalManager;
	}
}
