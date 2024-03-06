package io.github.fusionflux.portalcubed.framework.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class SimpleButtonWidget extends AbstractWidget {
	private final Sprites sprites;
	private final ButtonEvent onClick;
	private final ButtonEvent onRelease;

	public boolean pressed;

	public SimpleButtonWidget(int width, int height, Sprites sprites, ButtonEvent onClick, ButtonEvent onRelease) {
		super(0, 0, width, height, CommonComponents.EMPTY);
		this.sprites = sprites;
		this.onClick = onClick;
		this.onRelease = onRelease;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.blitSprite(sprites.choose(isHovered(), pressed, !active), getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		onClick.handle(this);
		pressed = true;
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		onRelease.handle(this);
		pressed = false;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		defaultButtonNarrationText(builder);
	}

	@FunctionalInterface
	public static interface ButtonEvent {
		void handle(SimpleButtonWidget button);
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
