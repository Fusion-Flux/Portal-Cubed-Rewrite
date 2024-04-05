package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.framework.gui.widget.ScrollbarWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.TickableWidget;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.List;

@ClientOnly
public interface ScreenExt {
	@Nullable List<TickableWidget> pc$tickables();
	@Nullable List<ScrollbarWidget> pc$scrollBars();
}
