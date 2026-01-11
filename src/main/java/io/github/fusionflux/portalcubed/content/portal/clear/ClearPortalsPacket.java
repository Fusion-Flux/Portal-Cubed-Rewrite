package io.github.fusionflux.portalcubed.content.portal.clear;

import java.util.HashSet;
import java.util.Set;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunSettings;
import io.github.fusionflux.portalcubed.content.portal.gun.skin.PortalGunSkin;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public enum ClearPortalsPacket implements ServerboundPacket {
	INSTANCE;

	public static final StreamCodec<ByteBuf, ClearPortalsPacket> CODEC = StreamCodec.unit(INSTANCE);

	public static final Component SUCCESS = Component.translatable("key.portalcubed.clear_portals.success");
	public static final Component DISABLED = Component.translatable("key.portalcubed.clear_portals.disabled");
	public static final Component NO_GUN = Component.translatable("key.portalcubed.clear_portals.no_gun");
	public static final Component FAIL = Component.translatable("key.portalcubed.clear_portals.fail");

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.CLEAR_PORTALS;
	}

	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		ServerPlayer player = ctx.player();

		if (!player.serverLevel().getGameRules().getBoolean(PortalCubedGameRules.MANUAL_PORTAL_CLEARING)) {
			player.sendSystemMessage(DISABLED, true);
			return;
		}

		boolean foundGun = false;
		boolean removedAny = false;
		// collect the skins of each portal that gets removed to tell the client what sounds to play
		Set<ResourceKey<PortalGunSkin>> removedSkins = new HashSet<>();

		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack held = player.getItemInHand(hand);
			PortalGunSettings gunSettings = held.get(PortalCubedDataComponents.PORTAL_GUN_SETTINGS);
			if (gunSettings == null)
				continue;

			foundGun = true;

			boolean removed = gunSettings.portals().join(
					primary -> remove(player, primary, Polarity.PRIMARY),
					secondary -> remove(player, secondary, Polarity.SECONDARY),
					(primaryRemoved, secondaryRemoved) -> primaryRemoved || secondaryRemoved
			);

			if (removed) {
				removedSkins.add(gunSettings.skinId());
				removedAny = true;
			}
		}

		if (!foundGun) {
			player.sendSystemMessage(NO_GUN, true);
		} else if (!removedAny) {
			player.sendSystemMessage(FAIL, true);
		} else {
			ServerPlayNetworking.send(player, new PortalsClearedPacket(removedSkins));
			player.sendSystemMessage(SUCCESS, true);
		}
	}

	private static boolean remove(ServerPlayer player, PortalSettings settings, Polarity polarity) {
		ServerPortalManager manager = player.serverLevel().portalManager();
		String key = settings.pairFor(player);
		PortalId id = new PortalId(key, polarity);
		PortalReference portal = manager.getPortal(id);

		if (portal != null) {
			manager.remove(portal);
			return true;
		}

		return false;
	}
}
