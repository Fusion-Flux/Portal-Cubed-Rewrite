package io.github.fusionflux.portalcubed.packet.serverbound;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignageBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.small.SmallSignageBlock;
import io.github.fusionflux.portalcubed.content.decoration.signage.small.SmallSignageBlockEntity;
import io.github.fusionflux.portalcubed.framework.signage.Signage;
import io.github.fusionflux.portalcubed.framework.signage.SignageManager;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class ConfigureSignageConfigPacket implements ServerboundPacket {
	protected final BlockPos signagePos;

	private ConfigureSignageConfigPacket(BlockPos signagePos) {
		this.signagePos = signagePos;
	}

	protected abstract void configure(@Nullable BlockEntity blockEntity);

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.signagePos);
	}

	@Override
	public final void handle(ServerPlayer player, PacketSender<CustomPacketPayload> responder) {
		Vec3 posCenter = Vec3.atBottomCenterOf(this.signagePos);
		if (!(player.getEyePosition().distanceToSqr(posCenter) > ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE)) {
			this.configure(player.level().getBlockEntity(this.signagePos));
		} else {
			PortalCubed.LOGGER.warn("Rejecting ConfigureSignageConfigPacket from {}: Location too far away from hit block {}.", player.getGameProfile().getName(), this.signagePos);
		}
	}

	public static final class Large extends ConfigureSignageConfigPacket {
		private final Signage.Holder signage;

		public Large(BlockPos signagePos, Signage.Holder signage) {
			super(signagePos);
			this.signage = signage;
		}

		public Large(FriendlyByteBuf buf) {
			this(buf.readBlockPos(), SignageManager.INSTANCE.get(buf.readResourceLocation()));
		}

		@Override
		protected void configure(@Nullable BlockEntity blockEntity) {
			if (blockEntity instanceof LargeSignageBlockEntity signageBlock)
				signageBlock.update(this.signage);
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			super.write(buf);
			buf.writeResourceLocation(this.signage.id());
		}

		@Override
		public ResourceLocation getId() {
			return PortalCubedPackets.CONFIGURE_LARGE_SIGNAGE;
		}
	}

	public static final class Small extends ConfigureSignageConfigPacket {
		private final SmallSignageBlock.Quadrant quadrant;
		private final TriState enabled;
		@Nullable
		private final Signage.Holder signage;

		public Small(BlockPos signagePos, SmallSignageBlock.Quadrant quadrant, TriState enabled, @Nullable Signage.Holder signage) {
			super(signagePos);
			this.quadrant = quadrant;
			this.enabled = enabled;
			this.signage = signage;
		}

		public Small(FriendlyByteBuf buf) {
			this(buf.readBlockPos(), buf.readEnum(SmallSignageBlock.Quadrant.class), buf.readEnum(TriState.class), SignageManager.INSTANCE.get(buf.readResourceLocation()));
		}

		@Override
		protected void configure(@Nullable BlockEntity blockEntity) {
			if (blockEntity instanceof SmallSignageBlockEntity signageBlock) {
				Level world = signageBlock.getLevel();
				if (this.enabled != TriState.DEFAULT && world != null) {
					BlockState state = signageBlock.getBlockState()
							.trySetValue(SmallSignageBlock.QUADRANT_PROPERTIES.get(this.quadrant), this.enabled.toBoolean());
					if (state != signageBlock.getBlockState())
						world.setBlock(this.signagePos, state, Block.UPDATE_ALL_IMMEDIATE);
				}

				if (this.signage != null)
					signageBlock.updateQuadrant(this.quadrant, this.signage);
			}
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			super.write(buf);
			buf.writeEnum(this.quadrant);
			buf.writeEnum(this.enabled);
			if (this.signage != null)
				buf.writeResourceLocation(this.signage.id());
		}

		@Override
		public ResourceLocation getId() {
			return PortalCubedPackets.CONFIGURE_SMALL_SIGNAGE;
		}
	}
}
