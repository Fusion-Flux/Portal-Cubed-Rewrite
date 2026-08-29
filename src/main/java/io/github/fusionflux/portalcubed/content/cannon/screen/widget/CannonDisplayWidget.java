package io.github.fusionflux.portalcubed.content.cannon.screen.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.item.ItemStack;

public class CannonDisplayWidget extends AbstractWidget {
	private final ItemStack item;
	private final float scale;

	public CannonDisplayWidget(int width, int height, ItemStack item) {
		super(0, 0, width, height, item.getDisplayName());
		this.item = item;
		this.scale = Math.min(width / 16f, height / 16f);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(this.getX(), this.getY());
		graphics.pose().scale(this.scale, this.scale);
		graphics.fakeItem(this.item, 0, 0);
		graphics.pose().popMatrix();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		builder.add(NarratedElementType.TITLE, this.getMessage());
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
		// don't
	}
}
