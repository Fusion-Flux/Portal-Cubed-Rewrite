package io.github.fusionflux.portalcubed.framework.gui.widget;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class TexturedStickyButton extends AbstractWidget {
	private final Textures textures;
	@Nullable
	private final Textures disabledTextures;
	private final Runnable onSelect;

	private boolean selected;

	public TexturedStickyButton(int x, int y, int width, int height, Component description,
								@Nullable Textures disabledTextures, Textures textures, Runnable onSelect) {
		super(x, y, width, height, description);
		this.textures = textures;
		this.disabledTextures = disabledTextures;
		this.onSelect = onSelect;
	}

	public TexturedStickyButton(int x, int y, int width, int height, Component description,
								Textures textures, Runnable onSelect) {
		this(x, y, width, height, description, null, textures, onSelect);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		Textures textures = this.isActive() ? this.textures : this.disabledTextures;
		if (textures != null)
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, textures.choose(this.isHovered(), this.selected), this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		if (!this.selected) {
			this.onSelect.run();
			this.selected = true;
		}
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

	public record Textures(Identifier texture, Identifier hovered, Identifier selected) {
		public Identifier choose(boolean hovered, boolean selected) {
			if (selected) {
				return this.selected;
			} else if (hovered) {
				return this.hovered;
			} else {
				return this.texture;
			}
		}

		public static Textures noHover(Identifier texture, Identifier selected) {
			return new Textures(texture, texture, selected);
		}
	}
}

