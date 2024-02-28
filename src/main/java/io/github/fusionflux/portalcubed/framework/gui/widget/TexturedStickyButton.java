package io.github.fusionflux.portalcubed.framework.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TexturedStickyButton extends AbstractWidget {
	private final Textures textures;
	private final Runnable onSelect;

	private boolean selected;

	public TexturedStickyButton(int x, int y, int width, int height, Component description,
								Textures textures, Runnable onSelect) {
		super(x, y, width, height, description);
		this.textures = textures;
		this.onSelect = onSelect;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		if (!this.isActive())
			return;
		ResourceLocation texture = this.textures.choose(this.isHovered(), this.selected);
		graphics.blitSprite(texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		this.onSelect.run();
		this.selected = true;
	}

	public void select() {
		this.selected = true;
	}

	public void deselect() {
		this.selected = false;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
	}

	public record Textures(ResourceLocation texture, ResourceLocation hovered, ResourceLocation selected) {
		public ResourceLocation choose(boolean hovered, boolean selected) {
			if (selected) {
				return this.selected;
			} else if (hovered) {
				return this.hovered;
			} else {
				return this.texture;
			}
		}

		public static Textures noHover(ResourceLocation texture, ResourceLocation selected) {
			return new Textures(texture, texture, selected);
		}
	}
}

