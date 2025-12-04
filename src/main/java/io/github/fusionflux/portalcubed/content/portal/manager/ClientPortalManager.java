package io.github.fusionflux.portalcubed.content.portal.manager;

import io.github.fusionflux.portalcubed.content.portal.manager.storage.SimplePortalStorage;
import net.minecraft.client.multiplayer.ClientLevel;

public class ClientPortalManager extends PortalManager {
	public ClientPortalManager(ClientLevel level) {
		super(new SimplePortalStorage(), level);
	}
}
