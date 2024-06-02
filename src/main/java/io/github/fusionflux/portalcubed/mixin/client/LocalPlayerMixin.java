package io.github.fusionflux.portalcubed.mixin.client;

import com.mojang.authlib.GameProfile;

import io.github.fusionflux.portalcubed.content.misc.SourcePhysics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
	public LocalPlayerMixin(ClientLevel world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(
			method = "aiStep",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"
			)
	)
	private void sourcePhysicsInput(CallbackInfo ci) {
		SourcePhysics.applyInput((LocalPlayer) (Object) this);
	}
}
