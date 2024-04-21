package io.github.fusionflux.portalcubed.content.portal.manager;

import net.minecraft.server.level.ServerLevel;

public class ServerPortalManager extends PortalManager {
	public final ServerLevel level;

	public ServerPortalManager(ServerLevel level) {
		super(level);
		this.level = level;
	}
}
