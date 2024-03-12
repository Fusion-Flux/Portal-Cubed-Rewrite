package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.framework.gui.widget.Tickable;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.List;

@ClientOnly
public interface ScreenExt {
	List<Tickable> pc$tickables();
}
