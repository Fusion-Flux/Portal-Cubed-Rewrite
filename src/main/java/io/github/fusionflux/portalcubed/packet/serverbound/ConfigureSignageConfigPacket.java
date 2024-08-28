package io.github.fusionflux.portalcubed.packet.serverbound;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignageBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PacketSender;

public abstract sealed class ConfigureSignageConfigPacket implements ServerboundPacket permits ConfigureSignageConfigPacket.Large {
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
}
