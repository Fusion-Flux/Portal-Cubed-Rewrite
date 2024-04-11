package io.github.fusionflux.portalcubed.framework.gui.widget;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SliderWidget extends AbstractWidget {
	public static final int BACKGROUND_HEIGHT = 14;
	public static final int HANDLE_WIDTH = 9;

	private final ResourceLocation sprite;
	private final ResourceLocation disabledSprite;
	private final WidgetSprites handleSprites;
	private final FloatConsumer onSlide;
	private final int bound;
	private float handlePos;

	public SliderWidget(ResourceLocation sprite, ResourceLocation disabledSprite, WidgetSprites handleSprites, int width, float defaultHandlePos, FloatConsumer onSlide) {
		super(0, 0, width, BACKGROUND_HEIGHT, CommonComponents.EMPTY);
		this.sprite = sprite;
		this.disabledSprite = disabledSprite;
		this.handleSprites = handleSprites;
		this.onSlide = onSlide;
		this.bound = width - HANDLE_WIDTH;
		this.handlePos = defaultHandlePos;
	}

	public SliderWidget(ResourceLocation sprite, int width, float defaultHandlePos, FloatConsumer onSlide) {
		this(sprite.withSuffix("_background"), sprite.withSuffix("_background_disabled"), new WidgetSprites(
				sprite.withSuffix("_handle"),
				sprite.withSuffix("_handle_disabled"),
				sprite.withSuffix("_handle_hover"),
				sprite.withSuffix("_handle_disabled_hover")
		), width, defaultHandlePos, onSlide);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		boolean active = isActive();
		graphics.blitSprite(active ? sprite : disabledSprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());

		int handleX = Mth.floor(handlePos * bound);
		graphics.blitSprite(handleSprites.get(active, isHovered()), this.getX() + handleX, this.getY(), HANDLE_WIDTH, BACKGROUND_HEIGHT);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		onDrag(mouseX, mouseY, 0, 0);
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
		double handleX = mouseX - getX() - ((double) HANDLE_WIDTH / 2);
		setHandlePos(Mth.clamp((float) (handleX / bound), 0, 1));
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
		// don't
	}

	private void setHandlePos(float handlePos) {
		if (this.handlePos != handlePos) {
			this.handlePos = handlePos;
			onSlide.accept(handlePos);
		}
	}

	public float handlePos() {
		return this.handlePos;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {

	}
}
