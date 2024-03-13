package io.github.fusionflux.portalcubed.framework.gui.widget;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public class ValueSelectButton<T extends StringRepresentable> extends AbstractWidget {
	private final WidgetSprites sprites;

	private final T widgetValue;
	private final Supplier<T> valueGetter;
	private final Consumer<T> valueSetter;
	private final Function<T, ValueSelectButton<T>> widgetMap;

	public ValueSelectButton(int width, int height, WidgetSprites sprites, T widgetValue, Supplier<T> valueGetter, Consumer<T> valueSetter, Function<T, ValueSelectButton<T>> widgetMap) {
		super(0, 0, width, height, CommonComponents.EMPTY);

		this.sprites = sprites;

		this.widgetValue = widgetValue;
		this.valueGetter = valueGetter;
		this.valueSetter = valueSetter;
		this.widgetMap = widgetMap;

		if (widgetValue == valueGetter.get()) active = false;
	}

	public ValueSelectButton(int width, int height, ResourceLocation baseSprite, T widgetValue, Supplier<T> valueGetter, Consumer<T> valueSetter, Function<T, ValueSelectButton<T>> widgetMap) {
		this(width, height, new WidgetSprites(
			getSpriteId(baseSprite, widgetValue, ""),
			getSpriteId(baseSprite, widgetValue, "pressed"),
			getSpriteId(baseSprite, widgetValue, "hover"),
			getSpriteId(baseSprite, widgetValue, "pressed_hover")
		), widgetValue, valueGetter, valueSetter, widgetMap);
	}

	private static ResourceLocation getSpriteId(ResourceLocation base, StringRepresentable value, String suffix) {
		base = base.withSuffix("_" + value.getSerializedName());
		return suffix.isEmpty() ? base : base.withSuffix("_" + suffix);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.blitSprite(sprites.get(isActive(), isHovered()), getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		var oldValue = valueGetter.get();
		valueSetter.accept(widgetValue);
		widgetMap.apply(oldValue).active = true;
		active = false;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		defaultButtonNarrationText(builder);
	}
}
