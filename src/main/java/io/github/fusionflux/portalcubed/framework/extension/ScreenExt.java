package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.framework.gui.widget.TickableWidget;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.List;

@ClientOnly
public interface ScreenExt {
	List<TickableWidget> pc$tickables();
}
