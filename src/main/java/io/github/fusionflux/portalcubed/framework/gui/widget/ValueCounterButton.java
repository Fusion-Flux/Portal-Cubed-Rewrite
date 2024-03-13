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

public class ValueCounterButton extends AbstractWidget {
	private static final int CLICKS_PER_SPEED = 5;
	private static final float MAX_CLICK_DELAY = 1f / 2f;
	private static final float MAX_CLICK_SPEED = 5f;

	protected final Sprites sprites;
	protected final int changeBy;
	protected final IntIntPair range;
	private final IntSupplier valueGetter;
	private final IntConsumer valueSetter;

	public boolean pressed;
	private int value;
	private int clickCounter;
	private float clickSpeed;
	private float clickDelay;

	public ValueCounterButton(int width, int height, Sprites sprites, int changeBy, IntIntPair range, IntSupplier valueGetter, IntConsumer valueSetter) {
		super(0, 0, width, height, CommonComponents.EMPTY);
		this.sprites = sprites;
		this.changeBy = changeBy;
		this.range = range;
		this.valueGetter = valueGetter;
		this.valueSetter = valueSetter;

		this.value = valueGetter.getAsInt();
		tick();
	}

	public ValueCounterButton(int width, int height, ResourceLocation baseSprite, int changeBy, IntIntPair range, IntSupplier valueGetter, IntConsumer valueSetter) {
		this(width, height, new Sprites(
			baseSprite, baseSprite.withSuffix("_hover"),
			baseSprite.withSuffix("_pressed"), baseSprite.withSuffix("_pressed_hover"),
			baseSprite.withSuffix("_disabled"), baseSprite.withSuffix("_disabled_hover")
		), changeBy, range, valueGetter, valueSetter);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.blitSprite(sprites.choose(isHovered(), pressed, !isActive()), getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		value = valueGetter.getAsInt();
		clickCounter = 0;
		clickSpeed = 1;
		clickDelay = MAX_CLICK_DELAY;
		click();
		pressed = true;
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		release();
		pressed = false;
	}

	protected void click() {
		value = Mth.clamp(value + changeBy, range.leftInt(), range.rightInt());
	}

	protected void release() {
		valueSetter.accept(value);
	}

	public void tick() {
		if (active && value == (changeBy < 0 ? range.leftInt() : range.rightInt())) {
			onRelease(0, 0);
			active = false;
		}

		if (pressed) {
			if (!isHovered()) {
				onRelease(0, 0);
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
