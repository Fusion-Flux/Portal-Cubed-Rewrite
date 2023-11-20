package io.github.fusionflux.portalcubed.content.portal.manager;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.framework.extension.ClientLevelExt;
import net.minecraft.client.multiplayer.ClientLevel;

public class ClientPortalManager extends PortalManager {
	public ClientPortalManager(ClientLevel level) {
		super(level);
	}

	public static ClientPortalManager of(ClientLevel level) {
		return ((ClientLevelExt) level).pc$clientPortalManager();
	}

	public void addPortal(Portal portal) {
		portal.findLinkedPortal(this);
		this.storage.addPortal(portal);
	}

	public void removePortal(int portalId) {
		Portal portal = storage.getByNetId(portalId);
		if (portal != null) {
			unlinkPortal(portal);
			this.storage.removePortal(portal);
		}
	}
}
