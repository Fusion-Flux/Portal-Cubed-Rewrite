package io.github.fusionflux.portalcubed.packet.serverbound;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignageBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.small.SmallSignageBlock;
import io.github.fusionflux.portalcubed.content.decoration.signage.small.SmallSignageBlockEntity;
import io.github.fusionflux.portalcubed.framework.signage.Signage;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public sealed interface ConfigureSignageConfigPacket extends ServerboundPacket permits ConfigureSignageConfigPacket.Large, ConfigureSignageConfigPacket.Small {
	Logger logger = LoggerFactory.getLogger(ConfigureSignageConfigPacket.class);

	BlockPos signagePos();

	void configure(@Nullable BlockEntity blockEntity);

	@Override
	default void handle(ServerPlayNetworking.Context ctx) {
		Player player = ctx.player();
		BlockPos signagePos = this.signagePos();
		if (player.canInteractWithBlock(signagePos, 1)) {
			this.configure(player.level().getBlockEntity(signagePos));
		} else {
			logger.warn("Rejecting packet from {}: Can't interact with block {}.", player.getGameProfile().getName(), signagePos);
		}
	}

	record Large(BlockPos signagePos, @Nullable Signage.Holder signage) implements ConfigureSignageConfigPacket {
		public static final StreamCodec<ByteBuf, Large> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, Large::signagePos,
				Signage.Holder.STREAM_CODEC, Large::signage,
				Large::new
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return PortalCubedPackets.CONFIGURE_LARGE_SIGNAGE;
		}

		@Override
		public void configure(@Nullable BlockEntity blockEntity) {
			if (blockEntity instanceof LargeSignageBlockEntity signageBlock)
				signageBlock.update(this.signage);
		}
	}

	record Small(BlockPos signagePos, SmallSignageBlock.Quadrant quadrant, TriState enabled, @Nullable Signage.Holder signage) implements ConfigureSignageConfigPacket {
		public static final StreamCodec<ByteBuf, Small> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, Small::signagePos,
				PortalCubedStreamCodecs.ofEnum(SmallSignageBlock.Quadrant.class), Small::quadrant,
				PortalCubedStreamCodecs.ofEnum(TriState.class), Small::enabled,
				Signage.Holder.STREAM_CODEC, Small::signage,
				Small::new
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return PortalCubedPackets.CONFIGURE_SMALL_SIGNAGE;
		}

		@Override
		public void configure(@Nullable BlockEntity blockEntity) {
			if (blockEntity instanceof SmallSignageBlockEntity signageBlock) {
				Level world = signageBlock.getLevel();
				if (this.enabled != TriState.DEFAULT && world != null)
					SmallSignageBlock.setQuadrant(world, this.signagePos, quadrant, this.enabled.toBoolean(true));
				if (this.signage != null)
					signageBlock.updateQuadrant(this.quadrant, this.signage);
			}
		}
	}
}
