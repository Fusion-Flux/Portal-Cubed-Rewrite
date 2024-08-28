package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignageBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.screen.LargeSignageConfigScreen;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

public abstract sealed class OpenSignageConfigPacket implements ClientboundPacket permits OpenSignageConfigPacket.Large {
	protected final BlockPos signagePos;

	private OpenSignageConfigPacket(BlockPos signagePos) {
		this.signagePos = signagePos;
	}

	private OpenSignageConfigPacket(FriendlyByteBuf buf) {
		this(buf.readBlockPos());
	}

	@ClientOnly
	@Nullable
	protected abstract Screen createScreen(@Nullable BlockEntity blockEntity);

	@Override
	public final void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.signagePos);
	}

	@ClientOnly
	@Override
	public final void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		Minecraft client = Minecraft.getInstance();
		Screen screen = this.createScreen(player.level().getBlockEntity(this.signagePos));
		if (client.screen == null && screen != null)
			client.setScreen(screen);
	}

	public static final class Large extends OpenSignageConfigPacket {
		public Large(BlockPos signagePos) {
			super(signagePos);
		}

		public Large(FriendlyByteBuf buf) {
			super(buf);
		}

		@Override
		@ClientOnly
		@Nullable
		protected Screen createScreen(@Nullable BlockEntity blockEntity) {
			return blockEntity instanceof LargeSignageBlockEntity signage ? new LargeSignageConfigScreen(signage) : null;
		}

		@Override
		public ResourceLocation getId() {
			return PortalCubedPackets.OPEN_LARGE_SIGNAGE_CONFIG;
		}
	}
}
