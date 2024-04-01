package io.github.fusionflux.portalcubed.framework.construct;

import io.github.fusionflux.portalcubed.content.cannon.ConstructRenderer;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConstructSyncPacket implements ClientboundPacket {
	private static final Logger logger = LoggerFactory.getLogger(ConstructSyncPacket.class);

	private final List<ConstructSet.Holder> constructs;

	public ConstructSyncPacket(Map<ResourceLocation, ConstructSet> constructs) {
		this.constructs = constructs.entrySet().stream().map(ConstructSet.Holder::new).toList();
	}

	public ConstructSyncPacket(FriendlyByteBuf buf) {
		int size = buf.readVarInt();
		this.constructs = new ArrayList<>(size);

		for (int i = 0; i < size; i++) {
			ResourceLocation id = buf.readResourceLocation();
			CompoundTag nbt = buf.readNbt();

			ConstructManager.tryParseConstruct(id, NbtOps.INSTANCE, nbt).ifPresent(this.constructs::add);
		}
	}

	@Override
	@ClientOnly
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		ConstructManager.INSTANCE.readFromPacket(this);
		ConstructRenderer.reload();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(this.constructs.size());
		this.constructs.forEach((holder) -> {
			ResourceLocation id = holder.id();
			ConstructSet.CODEC.encodeStart(NbtOps.INSTANCE, holder.constructSet()).get().ifLeft(nbt -> {
				buf.writeResourceLocation(id);
				buf.writeNbt(nbt);
			}).ifRight(
					partial -> logger.error("Failed to serialize construct {}: {}", id, partial.message())
			);
		});
	}

	public List<ConstructSet.Holder> getConstructs() {
		return constructs;
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.SYNC_CONSTRUCTS;
	}
}
