package io.github.fusionflux.portalcubed.packet.serverbound;

import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlockEntity;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock.Offset;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public record ConfigurePedestalButtonPacket(BlockPos pedestalButtonPos, int pressTime, Offset offset, boolean base) implements ServerboundPacket {
	public ConfigurePedestalButtonPacket(FriendlyByteBuf buf) {
		this(buf.readBlockPos(), buf.readVarInt(), buf.readEnum(Offset.class), buf.readBoolean());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(pedestalButtonPos);
		buf.writeVarInt(pressTime);
		buf.writeEnum(offset);
		buf.writeBoolean(base);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.CONFIGURE_PEDESTAL_BUTTON;
	}

	@Override
	public void handle(ServerPlayer player, PacketSender<CustomPacketPayload> responder) {
		var posCenter = Vec3.atBottomCenterOf(pedestalButtonPos);
		if (!(player.getEyePosition().distanceToSqr(posCenter) > ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE)) {
			var level = player.level();
			if (level.getBlockEntity(pedestalButtonPos) instanceof PedestalButtonBlockEntity pedestalButton) {
				var oldState = level.getBlockState(pedestalButtonPos);
				level.setBlock(pedestalButtonPos, oldState
					.setValue(PedestalButtonBlock.OFFSET, offset)
					.setValue(PedestalButtonBlock.BASE, base), Block.UPDATE_ALL);
				pedestalButton.setPressTime(pressTime);
			}
		} else {
			PortalCubed.LOGGER.warn("Rejecting ConfigurePedestalButtonPacket from {}: Location too far away from hit block {}.", player.getGameProfile().getName(), pedestalButtonPos);
		}
	}
}
