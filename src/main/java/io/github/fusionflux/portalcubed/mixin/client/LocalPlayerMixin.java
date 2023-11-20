package io.github.fusionflux.portalcubed.mixin.client;

import com.mojang.authlib.GameProfile;

import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;

import net.minecraft.core.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
	public LocalPlayerMixin(ClientLevel world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(method = "suffocatesAt", at = @At("HEAD"), cancellable = true)
	private void dontSuffocateInPortals(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (ClientPortalManager.of(this.clientLevel).getCollisionManager().modifiesCollisionAt(pos)) {
			cir.setReturnValue(false);
		}
	}
}
