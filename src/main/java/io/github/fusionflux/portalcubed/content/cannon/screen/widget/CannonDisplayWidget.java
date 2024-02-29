package io.github.fusionflux.portalcubed.content.cannon.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
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
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xFFaaaaaa);

		PoseStack matrices = graphics.pose();
		matrices.pushPose();

		matrices.scale(this.scale, this.scale, 1);
		matrices.translate(this.getX() / this.scale, this.getY() / this.scale, 0);
		graphics.renderFakeItem(this.item, 0, 0);
		matrices.popPose();
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
