package io.github.fusionflux.portalcubed.content.decoration.signage.screen.widget;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.decoration.signage.Signage;
import io.github.fusionflux.portalcubed.framework.gui.util.AdvancedTooltip;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SignageSlotWidget extends TexturedStickyButton {
	public static final int SIZE = 22;
	public static final int OFFSET = 3;

	public static final Textures DISABLED_TEXTURES = new Textures(
			PortalCubed.id("signage/slot_disabled"),
			PortalCubed.id("signage/slot_disabled_hover"),
			PortalCubed.id("signage/slot_disabled_selected")
	);
	public static final Textures TEXTURES = new Textures(
			PortalCubed.id("signage/slot"),
			PortalCubed.id("signage/slot_hover"),
			PortalCubed.id("signage/slot_selected")
	);

	private final ResourceLocation signageTexture;
	private final boolean small;
	private final AdvancedTooltip tooltip;

	public SignageSlotWidget(Signage signage, boolean small, boolean aged, Runnable onSelect) {
		this(signage, small, aged, 0, 0, onSelect);
	}

	public SignageSlotWidget(Signage signage, boolean small, boolean aged, int x, int y, Runnable onSelect) {
		super(x, y, SIZE, SIZE, signage.name(), DISABLED_TEXTURES, TEXTURES, onSelect);
		this.signageTexture = signage.selectTexture(aged);
		this.small = small;
		this.tooltip = new AdvancedTooltip(builder -> builder.add(signage.name()));
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderWidget(graphics, mouseX, mouseY, delta);
		int scale = this.small ? 2 : 1;
		graphics.blitSprite(RenderType::guiTextured, this.signageTexture, 16 * scale, 16 * scale, 0, 0, this.getX() + OFFSET, this.getY() + OFFSET, 16, 16);
		if (this.isHovered())
			this.tooltip.render(graphics, mouseX, mouseY);
	}
}
