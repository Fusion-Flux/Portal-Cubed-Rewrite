package io.github.fusionflux.portalcubed.framework.gui.widget;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

public class ValueSelectButton<T extends StringRepresentable> extends AbstractWidget {
	private final WidgetSprites sprites;

	private final T widgetValue;
	private final Supplier<T> valueGetter;
	private final Consumer<T> valueSetter;
	private final Function<T, ValueSelectButton<T>> widgetGetter;

	public ValueSelectButton(int width, int height, WidgetSprites sprites, T widgetValue, Supplier<T> valueGetter, Consumer<T> valueSetter, Function<T, ValueSelectButton<T>> widgetGetter) {
		super(0, 0, width, height, CommonComponents.EMPTY);

		this.sprites = sprites;

		this.widgetValue = widgetValue;
		this.valueGetter = valueGetter;
		this.valueSetter = valueSetter;
		this.widgetGetter = widgetGetter;

		if (widgetValue == valueGetter.get()) active = false;
	}

	public ValueSelectButton(int width, int height, Identifier baseSprite, T widgetValue, Supplier<T> valueGetter, Consumer<T> valueSetter, Function<T, ValueSelectButton<T>> widgetGetter) {
		this(width, height, new WidgetSprites(
			getSpriteId(baseSprite, widgetValue, ""),
			getSpriteId(baseSprite, widgetValue, "pressed"),
			getSpriteId(baseSprite, widgetValue, "hover"),
			getSpriteId(baseSprite, widgetValue, "pressed_hover")
		), widgetValue, valueGetter, valueSetter, widgetGetter);
	}

	private static Identifier getSpriteId(Identifier base, StringRepresentable value, String suffix) {
		base = base.withSuffix("_" + value.getSerializedName());
		return suffix.isEmpty() ? base : base.withSuffix("_" + suffix);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHovered()), getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		T oldValue = valueGetter.get();
		valueSetter.accept(widgetValue);
		widgetGetter.apply(oldValue).active = true;
		active = false;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		defaultButtonNarrationText(builder);
	}
}
