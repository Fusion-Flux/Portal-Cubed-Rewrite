package io.github.fusionflux.portalcubed.mixin;

import io.github.fusionflux.portalcubed.framework.extension.ClientboundTeleportEntityPacketExt;

import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientboundTeleportEntityPacket.class)
public class ClientboundTeleportEntityPacketMixin implements ClientboundTeleportEntityPacketExt {
	@Unique
	private boolean lerp = true;

	@Override
	public boolean pc$shouldLerp() {
		return lerp;
	}

	@Override
	public void pc$disableLerp() {
		this.lerp = false;
	}
}
