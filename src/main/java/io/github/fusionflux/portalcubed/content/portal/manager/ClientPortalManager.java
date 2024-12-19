package io.github.fusionflux.portalcubed.content.portal.manager;

import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import net.minecraft.client.multiplayer.ClientLevel;

public class ClientPortalManager extends PortalManager {
	public ClientPortalManager(ClientLevel level) {
		super(level);
	}

	public void setSyncedPair(String key, PortalPair pair) {
		this.setPair(key, pair);
	}
}
