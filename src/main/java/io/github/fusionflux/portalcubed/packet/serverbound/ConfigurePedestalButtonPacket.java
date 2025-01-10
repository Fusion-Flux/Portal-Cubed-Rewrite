package io.github.fusionflux.portalcubed.packet.serverbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock.Offset;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlockEntity;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record ConfigurePedestalButtonPacket(BlockPos pedestalButtonPos, int pressTime, Offset offset, boolean base) implements ServerboundPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurePedestalButtonPacket> CODEC = StreamCodec.composite(
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
		Player player = ctx.player();
		if (player.canInteractWithBlock(this.pedestalButtonPos, 1)) {
			Level world = player.level();
			if (world.getBlockEntity(this.pedestalButtonPos) instanceof PedestalButtonBlockEntity pedestalButton) {
				BlockState oldState = world.getBlockState(this.pedestalButtonPos);
				world.setBlock(this.pedestalButtonPos, oldState
						.setValue(PedestalButtonBlock.OFFSET, offset)
						.setValue(PedestalButtonBlock.BASE, base), Block.UPDATE_ALL);
				pedestalButton.setPressTime(pressTime);
			}
		} else {
			logger.warn("Rejecting packet from {}: Can't interact with block {}.", player.getGameProfile().getName(), this.pedestalButtonPos);
		}
	}
}
