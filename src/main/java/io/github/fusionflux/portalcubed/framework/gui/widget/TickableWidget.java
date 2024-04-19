package io.github.fusionflux.portalcubed.framework.gui.widget;

import org.quiltmc.loader.api.minecraft.ClientOnly;

@ClientOnly
public interface TickableWidget {
	void tick();
}
