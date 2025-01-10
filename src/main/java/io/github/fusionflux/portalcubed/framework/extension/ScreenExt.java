package io.github.fusionflux.portalcubed.framework.extension;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.gui.widget.ScrollbarWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.TickableWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ScreenExt {
	@Nullable List<TickableWidget> pc$tickables();
	@Nullable List<ScrollbarWidget> pc$scrollBars();
}
