package io.github.fusionflux.portalcubed.framework.gui.widget;

import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class TabWidget extends TexturedStickyButton {
	public static final int HEIGHT = 32;

	public TabWidget(int width, Component title, Textures textures, Runnable onSelect) {
		super(0, 0, width, HEIGHT, title, textures, onSelect);
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
		// don't
	}
}
