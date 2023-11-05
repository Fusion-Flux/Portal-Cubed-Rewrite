package io.github.fusionflux.portalcubed.content.portal.manager;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.framework.extension.ClientLevelExt;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;

public class ClientPortalManager extends PortalManager {
	private final ClientLevel level;

	public ClientPortalManager(ClientLevel level) {
		this.level = level;
	}

	public static ClientPortalManager of(ClientLevel level) {
		return ((ClientLevelExt) level).pc$portalManager();
	}

	public void addPortal(Portal portal) {
		this.storage.addPortal(portal);
	}
}
