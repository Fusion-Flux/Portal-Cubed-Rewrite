package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import io.github.fusionflux.portalcubed.content.portal.gun.GrabSoundManager;
import io.github.fusionflux.portalcubed.framework.extension.AbstractClientPlayerExt;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player implements AbstractClientPlayerExt {
	@Unique
	private final GrabSoundManager grabSoundManager = new GrabSoundManager(this);

	protected AbstractClientPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
		super(level, pos, yRot, gameProfile);
	}

	@Override
	public GrabSoundManager grabSoundManager() {
		return this.grabSoundManager;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void onTick(CallbackInfo ci) {
		this.grabSoundManager.tick();
	}
}
