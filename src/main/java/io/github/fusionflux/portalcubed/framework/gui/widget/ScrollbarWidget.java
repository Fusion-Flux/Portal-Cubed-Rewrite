package io.github.fusionflux.portalcubed.framework.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ScrollbarWidget extends AbstractWidget {
	public static final int BAR_HEIGHT = 88;
	public static final int SCROLLER_WIDTH = 12;
	public static final int SCROLLER_HEIGHT = 15;

	private final ResourceLocation sprite;
	private final ResourceLocation disabledSprite;
	private final Runnable onScroll;
	private float scrollPos;

	public ScrollbarWidget(ResourceLocation sprite, Runnable onScroll) {
		super(0, 0, SCROLLER_WIDTH, BAR_HEIGHT, CommonComponents.EMPTY);
		this.sprite = sprite;
		this.disabledSprite = sprite.withSuffix("_disabled");
		this.onScroll = onScroll;
	}

	public void scroll(double mouseY) {
		float oldScrollPos = this.scrollPos;
		this.scrollPos = Mth.clamp((float) ((mouseY - getY() - (SCROLLER_HEIGHT / 2)) / (BAR_HEIGHT - SCROLLER_HEIGHT)), 0, 1);
		if (this.scrollPos != oldScrollPos) onScroll.run();
	}

	public float scrollPos() {
		return this.scrollPos;
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
		scroll(mouseY);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		int yOffset = Mth.floor(scrollPos * (BAR_HEIGHT - SCROLLER_HEIGHT));
		graphics.blitSprite(isActive() ? this.sprite : this.disabledSprite, getX(), getY() + yOffset, SCROLLER_WIDTH, SCROLLER_HEIGHT);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
	}
}
