package io.github.fusionflux.portalcubed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.fusionflux.portalcubed.framework.extension.ClientboundTeleportEntityPacketExt;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;

@Mixin(ClientboundTeleportEntityPacket.class)
public class ClientboundTeleportEntityPacketMixin implements ClientboundTeleportEntityPacketExt {
	@Unique
	private boolean isLocal;

	@Override
	public void pc$setLocal(boolean value) {
		this.isLocal = value;
	}

	@Override
	public boolean pc$isLocal() {
		return this.isLocal;
	}
}
