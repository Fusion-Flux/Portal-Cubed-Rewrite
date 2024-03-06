package io.github.fusionflux.portalcubed.framework.gui.widget;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class SimpleButtonWidget extends AbstractWidget {
	private final Sprites sprites;
	private final Consumer<SimpleButtonWidget> onClick;
	private final Consumer<SimpleButtonWidget> onRelease;

	public SimpleButtonWidget(int width, int height, Sprites sprites, Consumer<SimpleButtonWidget> onClick, Consumer<SimpleButtonWidget> onRelease) {
		super(0, 0, width, height, CommonComponents.EMPTY);
		this.sprites = sprites;
		this.onClick = onClick;
		this.onRelease = onRelease;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		if (isActive())
			graphics.blitSprite(sprites.choose(isHovered()), getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		onClick.accept(this);
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		onRelease.accept(this);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if ((active && visible) && CommonInputs.selected(keyCode)) {
			playDownSound(Minecraft.getInstance().getSoundManager());
			onClick.accept(this);
			onRelease.accept(this);
			return true;
		}
		return false;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		defaultButtonNarrationText(builder);
	}

	public static record Sprites(ResourceLocation sprite, ResourceLocation hovered) {
		public ResourceLocation choose(boolean hovered) {
			return hovered ? this.hovered : this.sprite;
		}
	}
}
