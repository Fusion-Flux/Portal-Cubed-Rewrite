package io.github.fusionflux.portalcubed.packet.clientbound;

import java.util.OptionalInt;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.framework.extension.LevelExt;
import io.github.fusionflux.portalcubed.framework.util.PacketUtils;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record HoldStatusPacket(int holder, OptionalInt held) implements ClientboundPacket {
	public HoldStatusPacket(ServerPlayer holder, @Nullable HoldableEntity held) {
		this(holder.getId(), held == null ? OptionalInt.empty() : OptionalInt.of(held.getId()));
	}

	public HoldStatusPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt(), PacketUtils.readOptionalInt(buf));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(this.holder);
		PacketUtils.writeOptionalInt(buf, this.held);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.HOLD_STATUS;
	}

	@ClientOnly
	@Override
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		ClientLevel level = player.clientLevel;
		if (level.getEntity(this.holder) instanceof Player otherPlayer) {
			if (this.held.isPresent()) {
				Entity entity = level.getEntity(this.held.getAsInt());
				if (entity instanceof HoldableEntity holdable) {
					updateHeldEntity(otherPlayer, holdable);
				}
			} else {
				updateHeldEntity(otherPlayer, null);
			}
		}
	}

	private static void updateHeldEntity(Player player, @Nullable HoldableEntity held) {
		HoldableEntity oldHeld = player.getHeldEntity();
		player.setHeldEntity(held);

		// update sounds
		if (!player.getMainHandItem().is(PortalCubedItems.PORTAL_GUN))
			return;

		if (held != null && oldHeld == null && player.pc$grabSound() == null) { // grabbed, not currently playing a sound
			player.pc$grabSoundTimer(28);
			FollowingSoundInstance grabSound = new FollowingSoundInstance(PortalCubedSounds.PORTAL_GUN_GRAB, player.getSoundSource(), player);
			player.pc$grabSound(grabSound);
			((LevelExt) player.level()).pc$playSoundInstance(grabSound);
		} else if (held == null && oldHeld != null) { // dropped
			FollowingSoundInstance grabSound = (FollowingSoundInstance) player.pc$grabSound();
			if (grabSound != null) {
				grabSound.forceStop();
				player.pc$grabSound(null);
			}
			FollowingSoundInstance holdLoopSound = (FollowingSoundInstance) player.pc$holdLoopSound();
			if (holdLoopSound != null) {
				holdLoopSound.forceStop();
				player.pc$holdLoopSound(null);
			}
			player.playSound(PortalCubedSounds.PORTAL_GUN_DROP, 1, 1);
		}
	}
}
