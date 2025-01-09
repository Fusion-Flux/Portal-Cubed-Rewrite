package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Final;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import io.github.fusionflux.portalcubed.content.misc.SourcePhysics;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
	@Shadow
	@Final
	protected Minecraft minecraft;

	@Shadow
	public abstract void closeContainer();

	public LocalPlayerMixin(ClientLevel world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(method = "suffocatesAt", at = @At("HEAD"), cancellable = true)
	private void dontSuffocateInPortals(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		// disable suffocation when any portal modifies this position
		if (this.clientLevel.portalManager().isCollisionModified(pos)) {
			cir.setReturnValue(false);
		}
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

	@Override
	public void pc$disintegrateTick() {
		super.pc$disintegrateTick();

		// Copied from nether portal handling `handleNetherPortalClient` in `LocalPlayer`
		if (this.minecraft.screen != null && !this.minecraft.screen.isPauseScreen() && !(this.minecraft.screen instanceof DeathScreen)) {
			if (this.minecraft.screen instanceof AbstractContainerScreen) {
				this.closeContainer();
			}

			this.minecraft.setScreen(null);
		}
	}
}
