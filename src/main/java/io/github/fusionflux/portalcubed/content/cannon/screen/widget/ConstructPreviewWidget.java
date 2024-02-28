package io.github.fusionflux.portalcubed.content.cannon.screen.widget;

import io.github.fusionflux.portalcubed.content.cannon.screen.ConstructionCannonScreen;
import io.github.fusionflux.portalcubed.framework.construct.Construct;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ConstructPreviewWidget extends AbstractWidget {
	public static final Component MESSAGE = ConstructionCannonScreen.translate("construct_preview");

	private Construct construct;

	public ConstructPreviewWidget(int x, int y, int width, int height) {
		super(x, y, width, height, MESSAGE);
	}

	public void setConstruct(Construct construct) {
		this.construct = construct;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xFFaaaaaa);
		// render shadow
		// render construct
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		builder.add(NarratedElementType.TITLE, MESSAGE);
	}
}
