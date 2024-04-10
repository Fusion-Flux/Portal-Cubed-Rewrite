package io.github.fusionflux.portalcubed.content.cannon.screen.widget;

import io.github.fusionflux.portalcubed.content.cannon.screen.ConstructionCannonScreen;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton;
import net.minecraft.client.sounds.SoundManager;

public class TabWidget extends TexturedStickyButton {
	public static final int WIDTH = 58;
	public static final int HEIGHT = 32;

	public TabWidget(ConstructionCannonScreen.Tab tab, Runnable onSelect) {
		super(0, 0, WIDTH, HEIGHT, tab.title, tab.textures, onSelect);
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
		// don't
	}
}
