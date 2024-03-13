package io.github.fusionflux.portalcubed.framework.gui.widget;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ValueCounterButton extends AbstractWidget implements TickableWidget {
	private static final int CLICKS_PER_SPEED = 5;
	private static final float MAX_CLICK_DELAY = 1f / 2f;
	private static final float MAX_CLICK_SPEED = 5f;

	protected final Sprites sprites;
	protected final int changeBy;
	protected final IntIntPair range;
	private final IntSupplier valueGetter;
	private final IntConsumer valueSetter;
    private final Runnable onClickingStopped;

	public boolean pressed;
	private int clickCounter;
	private float clickSpeed;
	private float clickDelay;

	public ValueCounterButton(int width, int height, Sprites sprites, int changeBy, IntIntPair range, IntSupplier valueGetter, IntConsumer valueSetter, Runnable onClickingStopped) {
		super(0, 0, width, height, CommonComponents.EMPTY);
		this.sprites = sprites;
		this.changeBy = changeBy;
		this.range = range;
		this.valueGetter = valueGetter;
		this.valueSetter = valueSetter;
		this.onClickingStopped = onClickingStopped;

		tick();
	}

	public ValueCounterButton(int width, int height, ResourceLocation baseSprite, int changeBy, IntIntPair range, IntSupplier valueGetter, IntConsumer valueSetter, Runnable onClickingStopped) {
		this(width, height, new Sprites(
			baseSprite, baseSprite.withSuffix("_hover"),
			baseSprite.withSuffix("_pressed"), baseSprite.withSuffix("_pressed_hover"),
			baseSprite.withSuffix("_disabled"), baseSprite.withSuffix("_disabled_hover")
		), changeBy, range, valueGetter, valueSetter, onClickingStopped);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.blitSprite(sprites.choose(isHovered(), pressed, !isActive()), getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public final void onClick(double mouseX, double mouseY) {
		startClicking();
	}

	@Override
	public final void onRelease(double mouseX, double mouseY) {
		stopClicking();
	}

	protected void startClicking() {
		clickCounter = 0;
		clickSpeed = 1;
		clickDelay = MAX_CLICK_DELAY;
		click();
		pressed = true;
	}

	protected void click() {
		valueSetter.accept(Mth.clamp(valueGetter.getAsInt() + changeBy, range.leftInt(), range.rightInt()));
	}

	public void stopClicking() {
		onClickingStopped.run();
		pressed = false;
	}

	@Override
	public final void tick() {
		if (valueGetter.getAsInt() == (changeBy < 0 ? range.leftInt() : range.rightInt())) {
			stopClicking();
			active = false;
		} else {
			active = true;
		}

		if (pressed) {
			if (!isHovered()) {
				stopClicking();
				return;
			}
			clickDelay -= .1;
			if (clickDelay <= 0) {
				playDownSound(Minecraft.getInstance().getSoundManager());
				click();
				clickDelay = MAX_CLICK_DELAY / clickSpeed;
				if (++clickCounter == CLICKS_PER_SPEED) {
					clickSpeed = Math.min(clickSpeed + .4f, MAX_CLICK_SPEED);
					clickCounter = 0;
				}
			}
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		defaultButtonNarrationText(builder);
	}

	@FunctionalInterface
	public static interface ButtonEvent {
		void handle(ValueCounterButton button);
	}

	public static record Sprites(
		ResourceLocation sprite, ResourceLocation hovered,
		ResourceLocation pressed, ResourceLocation pressedHovered,
		ResourceLocation disabled, ResourceLocation disabledHovered
	) {
		public Sprites(ResourceLocation sprite) {
			this(
				sprite, sprite.withSuffix("_hover"),
				sprite.withSuffix("_pressed"), sprite.withSuffix("_pressed_hover"),
				sprite.withSuffix("_disabled"), sprite.withSuffix("_disabled_hover")
			);
		}

		public ResourceLocation choose(boolean hovered, boolean pressed, boolean disabled) {
			if (pressed) {
				return hovered ? this.pressedHovered : this.pressed;
			} else if (disabled) {
				return hovered ? this.disabledHovered : this.disabled;
			}
			return hovered ? this.hovered : this.sprite;
		}
	}
}
