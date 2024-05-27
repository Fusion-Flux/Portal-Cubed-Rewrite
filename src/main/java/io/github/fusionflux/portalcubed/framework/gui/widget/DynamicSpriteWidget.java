package io.github.fusionflux.portalcubed.framework.gui.widget;

import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class DynamicSpriteWidget<T> extends AbstractWidget {
	private final Supplier<T> valueGetter;
	private final Function<T, ResourceLocation> spriteGetter;

	public DynamicSpriteWidget(int width, int height, Supplier<T> valueGetter, Function<T, ResourceLocation> spriteGetter) {
		super(0, 0, width, height, CommonComponents.EMPTY);
		this.valueGetter = valueGetter;
		this.spriteGetter = spriteGetter;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.blitSprite(spriteGetter.apply(valueGetter.get()), getX(), getY(), getWidth(), getHeight());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Nullable
	@Override
	public ComponentPath nextFocusPath(FocusNavigationEvent event) {
		return null;
	}
}
