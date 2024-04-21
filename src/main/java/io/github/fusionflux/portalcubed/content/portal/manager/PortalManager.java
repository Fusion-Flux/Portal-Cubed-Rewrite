package io.github.fusionflux.portalcubed.content.portal.manager;

import net.minecraft.world.level.Level;

public abstract class PortalManager {
	private final Level level;

	public PortalManager(Level level) {
		this.level = level;
	}

}
