package io.github.fusionflux.portalcubed.framework.gui.widget;

import java.util.function.BooleanSupplier;

import org.apache.commons.lang3.function.BooleanConsumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class ToggleButton extends AbstractWidget {
	private final WidgetSprites sprites;

	private final BooleanSupplier valueGetter;
	private final BooleanConsumer valueSetter;

	public ToggleButton(int width, int height, WidgetSprites sprites, BooleanSupplier valueGetter, BooleanConsumer valueSetter) {
		super(0, 0, width, height, CommonComponents.EMPTY);

		this.sprites = sprites;

		this.valueGetter = valueGetter;
		this.valueSetter = valueSetter;
	}

	public ToggleButton(int width, int height, ResourceLocation baseSprite, BooleanSupplier valueGetter, BooleanConsumer valueSetter) {
		this(width, height, new WidgetSprites(
			baseSprite.withSuffix("_enabled"),
			baseSprite.withSuffix("_disabled"),
			baseSprite.withSuffix("_enabled_hover"),
			baseSprite.withSuffix("_disabled_hover")
		), valueGetter, valueSetter);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.blitSprite(sprites.get(valueGetter.getAsBoolean(), isHovered()), getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		valueSetter.accept(!valueGetter.getAsBoolean());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		defaultButtonNarrationText(builder);
	}
}
