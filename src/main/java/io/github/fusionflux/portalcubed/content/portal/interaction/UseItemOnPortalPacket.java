package io.github.fusionflux.portalcubed.content.portal.interaction;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

public record UseItemOnPortalPacket(PortalId portal, InteractionHand hand) implements ServerboundPacket {
	public static final StreamCodec<ByteBuf, UseItemOnPortalPacket> CODEC = StreamCodec.composite(
			PortalId.STREAM_CODEC, UseItemOnPortalPacket::portal,
			PortalCubedStreamCodecs.HAND, UseItemOnPortalPacket::hand,
			UseItemOnPortalPacket::new
	);

	private static final Logger logger = LogUtils.getLogger();

	/**
	 * @see ServerGamePacketListenerImpl#handleUseItem(ServerboundUseItemPacket)
	 */
	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		ServerPlayer player = ctx.player();
		if (!player.hasClientLoaded())
			return;

		player.resetLastActionTime();
		ItemStack stack = player.getItemInHand(this.hand);
		if (stack.isEmpty())
			return;

		ServerLevel level = player.serverLevel();
		if (!stack.isItemEnabled(level.enabledFeatures()))
			return;

		if (!(stack.getItem() instanceof UsableOnPortals item)) {
			logger.error("Invalid UseItemOnPortal packet: {} does not implement UsableOnPortals", stack.getItem());
			return;
		}

		PortalReference portal = level.portalManager().getPortal(this.portal);
		if (portal == null) {
			logger.error("Invalid UseItemOnPortal packet: portal {} does not exist", this.portal);
			return;
		}

		InteractionResult result = item.useOnPortal(player, portal, stack, this.hand);
		if (result instanceof InteractionResult.Success success && success.swingSource() == InteractionResult.SwingSource.SERVER) {
			player.swing(this.hand, true);
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.USE_ITEM_ON_PORTAL;
	}
}
