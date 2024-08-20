package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignagePanelBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignagePanelConfigScreen;
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

public abstract class OpenSignagePanelConfigPacket implements ClientboundPacket {
	protected final BlockPos signagePanelPos;

	private OpenSignagePanelConfigPacket(BlockPos signagePanelPos) {
		this.signagePanelPos = signagePanelPos;
	}

	private OpenSignagePanelConfigPacket(FriendlyByteBuf buf) {
		this(buf.readBlockPos());
	}

	@ClientOnly
	@Nullable
	protected abstract Screen createScreen(@Nullable BlockEntity blockEntity);

	@Override
	public final void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.signagePanelPos);
	}

	@ClientOnly
	@Override
	public final void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		Minecraft client = Minecraft.getInstance();
		Screen screen = this.createScreen(player.level().getBlockEntity(this.signagePanelPos));
		if (client.screen == null && screen != null)
			client.setScreen(screen);
	}

	public static final class Large extends OpenSignagePanelConfigPacket {
		public Large(BlockPos signagePanelPos) {
			super(signagePanelPos);
		}

		public Large(FriendlyByteBuf buf) {
			super(buf);
		}

		@Override
		@ClientOnly
		@Nullable
		protected Screen createScreen(@Nullable BlockEntity blockEntity) {
			return blockEntity instanceof LargeSignagePanelBlockEntity signagePanel ? new LargeSignagePanelConfigScreen(signagePanel) : null;
		}

		@Override
		public ResourceLocation getId() {
			return PortalCubedPackets.OPEN_LARGE_SIGNAGE_PANEL_CONFIG;
		}
	}
}
