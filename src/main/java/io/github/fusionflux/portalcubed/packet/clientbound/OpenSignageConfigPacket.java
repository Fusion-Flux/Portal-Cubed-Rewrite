package io.github.fusionflux.portalcubed.packet.clientbound;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignageBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.screen.LargeSignageConfigScreen;
import io.github.fusionflux.portalcubed.content.decoration.signage.screen.SmallSignageConfigScreen;
import io.github.fusionflux.portalcubed.content.decoration.signage.small.SmallSignageBlock;
import io.github.fusionflux.portalcubed.content.decoration.signage.small.SmallSignageBlockEntity;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public interface OpenSignageConfigPacket extends ClientboundPacket {
	Logger logger = LoggerFactory.getLogger(OpenSignageConfigPacket.class);

	BlockPos signagePos();

	@Environment(EnvType.CLIENT)
	@Nullable
	Screen createScreen(@Nullable BlockEntity blockEntity);

	@Environment(EnvType.CLIENT)
	@Override
	default void handle(ClientPlayNetworking.Context ctx) {
		Minecraft client = ctx.client();
		Screen screen = this.createScreen(client.player.level().getBlockEntity(this.signagePos()));
		if (client.screen == null && screen != null)
			client.setScreen(screen);
	}

	record Large(BlockPos signagePos) implements OpenSignageConfigPacket {
		public static final StreamCodec<RegistryFriendlyByteBuf, Large> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, Large::signagePos,
				Large::new
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return PortalCubedPackets.OPEN_LARGE_SIGNAGE_CONFIG;
		}

		@Override
		@Environment(EnvType.CLIENT)
		@Nullable
		public Screen createScreen(@Nullable BlockEntity blockEntity) {
			return blockEntity instanceof LargeSignageBlockEntity signage ? new LargeSignageConfigScreen(signage) : null;
		}
	}

	record Small(BlockHitResult hit) implements OpenSignageConfigPacket {
		public static final StreamCodec<RegistryFriendlyByteBuf, Small> CODEC = StreamCodec.composite(
				PortalCubedStreamCodecs.BLOCK_HIT_RESULT, Small::hit,
				Small::new
		);

		@Override
		public BlockPos signagePos() {
			return this.hit.getBlockPos();
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return PortalCubedPackets.OPEN_SMALL_SIGNAGE_CONFIG;
		}

		@Override
		@Environment(EnvType.CLIENT)
		@Nullable
		public Screen createScreen(@Nullable BlockEntity blockEntity) {
			if (blockEntity instanceof SmallSignageBlockEntity signage)
				return SmallSignageBlock
						.getHitQuadrant(signage.getBlockState(), this.hit)
						.map(quadrant -> new SmallSignageConfigScreen(signage, quadrant))
						.orElse(null);
			return null;
		}
	}
}
