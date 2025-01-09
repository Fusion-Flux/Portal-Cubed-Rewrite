package io.github.fusionflux.portalcubed.framework.extension;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import io.github.fusionflux.portalcubed.framework.gui.widget.ScrollbarWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.TickableWidget;

@ClientOnly
public interface ScreenExt {
	@Nullable List<TickableWidget> pc$tickables();
	@Nullable List<ScrollbarWidget> pc$scrollBars();
}
