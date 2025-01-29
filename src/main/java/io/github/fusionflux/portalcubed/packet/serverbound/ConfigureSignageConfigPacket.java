package io.github.fusionflux.portalcubed.packet.serverbound;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fusionflux.portalcubed.content.PortalCubedCriteriaTriggers;
import io.github.fusionflux.portalcubed.content.PortalCubedTestElementSettings;
import io.github.fusionflux.portalcubed.content.decoration.signage.Signage;
import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignageBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.small.SmallSignageBlock;
import io.github.fusionflux.portalcubed.content.decoration.signage.small.SmallSignageBlockEntity;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public sealed interface ConfigureSignageConfigPacket extends ServerboundPacket permits ConfigureSignageConfigPacket.Large, ConfigureSignageConfigPacket.Small {
	Logger logger = LoggerFactory.getLogger(ConfigureSignageConfigPacket.class);

	BlockPos signagePos();

	void configure(ServerPlayer player, @Nullable BlockEntity blockEntity);

	@Override
	default void handle(ServerPlayNetworking.Context ctx) {
		ServerPlayer player = ctx.player();
		BlockPos signagePos = this.signagePos();
		if (player.canInteractWithBlock(signagePos, 1)) {
			this.configure(player, player.level().getBlockEntity(signagePos));
		} else {
			logger.warn("Rejecting packet from {}: Can't interact with block {}.", player.getGameProfile().getName(), signagePos);
		}
	}

	record Large(BlockPos signagePos, Holder<Signage> image) implements ConfigureSignageConfigPacket {
		public static final StreamCodec<RegistryFriendlyByteBuf, Large> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, Large::signagePos,
				Signage.LARGE_STREAM_CODEC, Large::image,
				Large::new
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return PortalCubedPackets.CONFIGURE_LARGE_SIGNAGE;
		}

		@Override
		public void configure(ServerPlayer player, @Nullable BlockEntity blockEntity) {
			if (blockEntity instanceof LargeSignageBlockEntity signageBlock) {
				signageBlock.setImage(this.image);
				PortalCubedCriteriaTriggers.CONFIGURE_TEST_ELEMENT.trigger(
						player,
						Set.of(PortalCubedTestElementSettings.LARGE_SIGNAGE_IMAGE)
				);
			}
		}
	}

	record Small(BlockPos signagePos, SmallSignageBlock.Quadrant quadrant, TriState enabled, @Nullable Holder<Signage> image) implements ConfigureSignageConfigPacket {
		public static final StreamCodec<RegistryFriendlyByteBuf, Small> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, Small::signagePos,
				SmallSignageBlock.Quadrant.STREAM_CODEC, Small::quadrant,
				PortalCubedStreamCodecs.ofEnum(TriState.class), Small::enabled,
				PortalCubedStreamCodecs.nullable(Signage.SMALL_STREAM_CODEC), Small::image,
				Small::new
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return PortalCubedPackets.CONFIGURE_SMALL_SIGNAGE;
		}

		@Override
		public void configure(ServerPlayer player, @Nullable BlockEntity blockEntity) {
			if (blockEntity instanceof SmallSignageBlockEntity signageBlock) {
				Level world = signageBlock.getLevel();
				Set<ResourceLocation> changedSettings = new HashSet<>();

				if (this.enabled != TriState.DEFAULT && world != null) {
					SmallSignageBlock.setQuadrant(world, this.signagePos, this.quadrant, this.enabled.toBoolean(true));
					changedSettings.add(PortalCubedTestElementSettings.SMALL_SIGNAGE_QUADRANT_TOGGLE);
				}

				if (this.image != null) {
					signageBlock.setQuadrantImage(this.quadrant, this.image);
					changedSettings.add(PortalCubedTestElementSettings.SMALL_SIGNAGE_IMAGE);
				}

				if (!changedSettings.isEmpty()) {
					PortalCubedCriteriaTriggers.CONFIGURE_TEST_ELEMENT.trigger(player, changedSettings);
				}
			}
		}
	}
}
