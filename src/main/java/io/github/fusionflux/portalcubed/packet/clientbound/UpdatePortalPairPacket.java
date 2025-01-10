package io.github.fusionflux.portalcubed.packet.clientbound;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record UpdatePortalPairPacket(String key, @Nullable PortalPair pair) implements ClientboundPacket {
	public UpdatePortalPairPacket(FriendlyByteBuf buf) {
		this(buf.readUtf(), buf.readNullable(buffer -> buf.readJsonWithCodec(PortalPair.CODEC)));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(this.key);
		buf.writeNullable(this.pair, (buffer, pair) -> buffer.writeJsonWithCodec(PortalPair.CODEC, pair));
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.UPDATE_PORTAL_PAIR;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handle(ClientPlayNetworking.Context ctx) {
		ClientPortalManager manager = player.clientLevel.portalManager();
		manager.setSyncedPair(this.key, this.pair);
	}
}
