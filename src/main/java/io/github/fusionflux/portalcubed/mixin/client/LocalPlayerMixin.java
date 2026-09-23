package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;

import io.github.fusionflux.portalcubed.content.boots.SourcePhysics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec2;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
	@Shadow
	@Final
	protected Minecraft minecraft;

	@Shadow
	public abstract void closeContainer();

	protected LocalPlayerMixin(ClientLevel world, GameProfile profile) {
		super(world, profile);
	}

	@ModifyExpressionValue(
			method = "applyInput",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;modifyInput(Lnet/minecraft/world/phys/Vec2;)Lnet/minecraft/world/phys/Vec2;"
			)
	)
	private Vec2 sourcePhysicsInput(Vec2 original) {
		return SourcePhysics.applyInput((LocalPlayer) (Object) this, original);
	}
}
