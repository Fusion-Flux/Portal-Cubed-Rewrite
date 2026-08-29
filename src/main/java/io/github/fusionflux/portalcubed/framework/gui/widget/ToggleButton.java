package io.github.fusionflux.portalcubed.framework.gui.widget;

import java.util.function.BooleanSupplier;

import org.apache.commons.lang3.function.BooleanConsumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;

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

	public ToggleButton(int width, int height, Identifier baseSprite, BooleanSupplier valueGetter, BooleanConsumer valueSetter) {
		this(width, height, new WidgetSprites(
			baseSprite.withSuffix("_enabled"),
			baseSprite.withSuffix("_disabled"),
			baseSprite.withSuffix("_enabled_hover"),
			baseSprite.withSuffix("_disabled_hover")
		), valueGetter, valueSetter);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprites.get(valueGetter.getAsBoolean(), isHovered()), getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		valueSetter.accept(!valueGetter.getAsBoolean());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		defaultButtonNarrationText(builder);
	}
}
