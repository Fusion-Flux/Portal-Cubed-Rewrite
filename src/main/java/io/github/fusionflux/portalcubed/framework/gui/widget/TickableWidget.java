package io.github.fusionflux.portalcubed.framework.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface TickableWidget {
	void tick();
}
