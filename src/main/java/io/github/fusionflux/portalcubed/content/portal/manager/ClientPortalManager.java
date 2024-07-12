package io.github.fusionflux.portalcubed.content.portal.manager;

import java.util.UUID;

import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import net.minecraft.client.multiplayer.ClientLevel;

public class ClientPortalManager extends PortalManager {
	public ClientPortalManager(ClientLevel level) {
		super(level);
	}

	public void setSyncedPair(UUID id, PortalPair pair) {
		this.setPair(id, pair);
	}
}
