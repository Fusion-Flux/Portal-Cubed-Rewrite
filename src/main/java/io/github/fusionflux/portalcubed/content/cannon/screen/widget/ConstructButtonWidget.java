package io.github.fusionflux.portalcubed.content.cannon.screen.widget;

import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ConstructButtonWidget extends TexturedStickyButton {
	public static final int SIZE = 20;

	public ConstructButtonWidget(Component description, Runnable onSelect) {
		super(0, 0, SIZE, SIZE, description, null, onSelect);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

		// don't call super, no texture
	}
}
