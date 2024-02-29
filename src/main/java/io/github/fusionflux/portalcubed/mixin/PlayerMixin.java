package io.github.fusionflux.portalcubed.mixin;

import java.util.OptionalInt;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.networking.api.PlayerLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.PropHoldPacket;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public class PlayerMixin implements PlayerExt {
	@Unique private OptionalInt heldProp = OptionalInt.empty();

	@ClientOnly @Unique private int grabSoundTimer = 0;
	@ClientOnly @Unique @Nullable private FollowingSoundInstance grabSound = null;
	@ClientOnly @Unique @Nullable private FollowingSoundInstance holdLoopSound = null;

	@ClientOnly
	private void clientOnHeldPropUpdate(Player self, OptionalInt newHeldProp) {
		if (!self.getMainHandItem().is(PortalCubedItems.PORTAL_GUN)) return;

		if (newHeldProp.isPresent() && heldProp.isEmpty() && grabSound == null) {
			grabSoundTimer = 28;
			grabSound = new FollowingSoundInstance(PortalCubedSounds.PORTAL_GUN_GRAB, self.getSoundSource(), self);
			Minecraft.getInstance().getSoundManager().play(grabSound);
		} else if (newHeldProp.isEmpty() && heldProp.isPresent()) {
			if (grabSound != null) {
				grabSound.forceStop();
				grabSound = null;
			}
			if (holdLoopSound != null) {
				holdLoopSound.forceStop();
				holdLoopSound = null;
			}
			self.playSound(PortalCubedSounds.PORTAL_GUN_DROP, 1, 1);
		}
	}

	@Override
	public void pc$heldProp(OptionalInt prop) {
		if ((Object) this instanceof ServerPlayer self) {
			var packet = new PropHoldPacket(self.getId(), prop);
			for (var trackingPlayer : PlayerLookup.tracking(self))
				PortalCubedPackets.sendToClient(trackingPlayer, packet);
			PortalCubedPackets.sendToClient(self, packet);
		} else if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			clientOnHeldPropUpdate((Player) (Object) this, prop);
		}
		heldProp = prop;
	}

	@Override
	public OptionalInt pc$heldProp() {
		return heldProp;
	}


	@Override
	public void pc$grabSoundTimer(int timer) {
		grabSoundTimer = timer;
	}

	@Override
	public int pc$grabSoundTimer() {
		return grabSoundTimer;
	}

	@Override
	public void pc$grabSound(Object grabSound) {
		this.grabSound = (FollowingSoundInstance) grabSound;
	}

	@Override
	public Object pc$grabSound() {
		return grabSound;
	}

	@Override
	public void pc$holdLoopSound(Object holdLoopSound) {
		this.holdLoopSound = (FollowingSoundInstance) holdLoopSound;
	}

	@Override
	public Object pc$holdLoopSound() {
		return holdLoopSound;
	}
}
