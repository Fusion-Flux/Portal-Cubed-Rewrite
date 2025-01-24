package io.github.fusionflux.portalcubed.packet.serverbound;

import io.github.fusionflux.portalcubed.content.PortalCubedCriteriaTriggers;
import io.github.fusionflux.portalcubed.content.PortalCubedTestElementSettings;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.level.ServerPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock.Offset;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlockEntity;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public record ConfigurePedestalButtonPacket(BlockPos pedestalButtonPos, int pressTime, Offset offset, boolean base) implements ServerboundPacket {
	public static final StreamCodec<ByteBuf, ConfigurePedestalButtonPacket> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, ConfigurePedestalButtonPacket::pedestalButtonPos,
			ByteBufCodecs.VAR_INT, ConfigurePedestalButtonPacket::pressTime,
			PortalCubedStreamCodecs.ofEnum(Offset.class), ConfigurePedestalButtonPacket::offset,
			ByteBufCodecs.BOOL, ConfigurePedestalButtonPacket::base,
			ConfigurePedestalButtonPacket::new
	);

	private static final Logger logger = LoggerFactory.getLogger(ConfigurePedestalButtonPacket.class);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.CONFIGURE_PEDESTAL_BUTTON;
	}

	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		ServerPlayer player = ctx.player();
		if (!player.canInteractWithBlock(this.pedestalButtonPos, 1)) {
			logger.warn("Rejecting packet from {}: Can't interact with block {}.", player.getGameProfile().getName(), this.pedestalButtonPos);
			return;
		}

		Level world = player.level();
		if (world.getBlockEntity(this.pedestalButtonPos) instanceof PedestalButtonBlockEntity pedestalButton) {
			BlockState state = world.getBlockState(this.pedestalButtonPos);
			Set<ResourceLocation> changedSettings = new HashSet<>();

			if (this.offset != state.getValue(PedestalButtonBlock.OFFSET)) {
				state = state.setValue(PedestalButtonBlock.OFFSET, this.offset);
				changedSettings.add(PortalCubedTestElementSettings.PEDESTAL_BUTTON_BASE_POSITION);
			}

			if (this.base != state.getValue(PedestalButtonBlock.BASE)) {
				state = state.setValue(PedestalButtonBlock.BASE, this.base);
				changedSettings.add(PortalCubedTestElementSettings.PEDESTAL_BUTTON_BASE_TOGGLE);
			}

			if (!changedSettings.isEmpty()) {
				world.setBlock(this.pedestalButtonPos, state, Block.UPDATE_ALL);
			}

			if (this.pressTime != pedestalButton.getPressTime()) {
				changedSettings.add(PortalCubedTestElementSettings.PEDESTAL_BUTTON_TIMER);
				pedestalButton.setPressTime(this.pressTime);
			}

			PortalCubedCriteriaTriggers.CONFIGURE_TEST_ELEMENT.trigger(player, changedSettings);
		}
	}
}
