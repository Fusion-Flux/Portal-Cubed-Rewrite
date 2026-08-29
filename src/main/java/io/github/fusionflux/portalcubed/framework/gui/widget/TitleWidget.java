package io.github.fusionflux.portalcubed.framework.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class TitleWidget extends AbstractWidget {
	public static final int COLOR = 4210752;

	private final Font font;

	public TitleWidget(Component title, Font font) {
		super(0, 0, font.width(title.getVisualOrderText()), 9, title);
		this.font = font;
		this.active = false;
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.text(this.font, this.getMessage().getVisualOrderText(), this.getX(), this.getY(), COLOR, false);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {

	}
}
