package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import io.github.fusionflux.portalcubed.content.misc.LemonadeItem;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
	private void stopUsingItemEarlyIfLemonade(ServerboundPlayerActionPacket packet, CallbackInfo ci, @Share("lemonade") LocalBooleanRef lemonade) {
		ItemStack useItem = player.getUseItem();
		if (LemonadeItem.isArmed(useItem)) {
			lemonade.set(true);
			player.stopUsingItem();
		}
	}

	@WrapWithCondition(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;stopUsingItem()V"))
	private boolean cancelLateStopUsingItem(ServerPlayer instance, @Share("lemonade") LocalBooleanRef lemonade) {
		return !lemonade.get();
	}
}
