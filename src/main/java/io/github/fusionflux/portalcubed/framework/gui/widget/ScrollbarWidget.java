package io.github.fusionflux.portalcubed.framework.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ScrollbarWidget extends AbstractWidget {
	public static final int BAR_HEIGHT = 88;
	public static final int SCROLLER_WIDTH = 12;
	public static final int SCROLLER_HEIGHT = 15;
	public static final int SCROLLER_BOUND = BAR_HEIGHT - SCROLLER_HEIGHT;

	private final ResourceLocation sprite;
	private final ResourceLocation disabledSprite;
	private final Runnable onScroll;
	private float scrollPos;

	public float scrollRate;

	public ScrollbarWidget(ResourceLocation sprite, Runnable onScroll) {
		super(0, 0, SCROLLER_WIDTH, BAR_HEIGHT, CommonComponents.EMPTY);
		this.sprite = sprite;
		this.disabledSprite = sprite.withSuffix("_disabled");
		this.onScroll = onScroll;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		int yOffset = Mth.floor(scrollPos * SCROLLER_BOUND);
		graphics.blitSprite(this.isActive() ? this.sprite : this.disabledSprite, this.getX(), this.getY() + yOffset, SCROLLER_WIDTH, SCROLLER_HEIGHT);
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
		double scrollerY = mouseY - getY() - (SCROLLER_HEIGHT / 2);
		setScrollPos(Mth.clamp((float) (scrollerY / SCROLLER_BOUND), 0, 1));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount, double d) {
		if (!this.isActive())
			return false;
		float newScrollPos = (float) (this.scrollPos - (this.scrollRate * d));
		return setScrollPos(Mth.clamp(newScrollPos, 0, 1));
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
		// don't
	}

	private boolean setScrollPos(float scrollPos) {
		if (this.scrollPos != scrollPos) {
			this.scrollPos = scrollPos;
			onScroll.run();
			return true;
		}
		return false;
	}

	public float scrollPos() {
		return this.scrollPos;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
	}
}
