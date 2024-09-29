package io.github.fusionflux.portalcubed.framework.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.network.chat.Component;

public class TitleWidget extends AbstractStringWidget {
	public static final int COLOR = 4210752;

	public TitleWidget(Component title, Font renderer) {
		super(0, 0, renderer.width(title.getVisualOrderText()), 9, title, renderer);
		this.setColor(COLOR);
		this.active = false;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.drawString(getFont(), getMessage().getVisualOrderText(), getX(), getY(), getColor(), false);
	}
}
