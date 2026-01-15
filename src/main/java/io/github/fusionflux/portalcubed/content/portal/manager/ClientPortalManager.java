package io.github.fusionflux.portalcubed.content.portal.manager;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.packet.clientbound.UpdatePortalPairPacket;
import net.minecraft.client.multiplayer.ClientLevel;

public final class ClientPortalManager extends PortalManager {
	public ClientPortalManager(ClientLevel level) {
		super(level);
	}

	/**
	 * Only intended to be called from {@link UpdatePortalPairPacket}
	 */
	@Override
	@ApiStatus.Internal
	public void setPair(String key, @Nullable PortalPair newPair) {
		super.setPair(key, newPair);
	}
}
