package io.github.fusionflux.portalcubed.packet.clientbound;

import java.util.OptionalInt;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record HoldStatusPacket(int holder, OptionalInt held) implements ClientboundPacket {
	public static final StreamCodec<ByteBuf, HoldStatusPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, HoldStatusPacket::holder,
			ByteBufCodecs.OPTIONAL_VAR_INT, HoldStatusPacket::held,
			HoldStatusPacket::new
	);

	public HoldStatusPacket(ServerPlayer holder, @Nullable HoldableEntity held) {
		this(holder.getId(), held == null ? OptionalInt.empty() : OptionalInt.of(held.getId()));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.HOLD_STATUS;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handle(ClientPlayNetworking.Context ctx) {
		ClientLevel level = ctx.player().clientLevel;
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
			player.level().pc$playSoundInstance(grabSound);
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
