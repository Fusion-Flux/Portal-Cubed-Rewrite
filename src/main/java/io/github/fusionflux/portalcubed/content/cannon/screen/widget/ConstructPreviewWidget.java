package io.github.fusionflux.portalcubed.content.cannon.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.content.cannon.screen.CannonDataHolder;
import io.github.fusionflux.portalcubed.content.cannon.screen.ConstructionCannonScreen;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Rotation;

public class ConstructPreviewWidget extends AbstractWidget {
	public static final Component MESSAGE = ConstructionCannonScreen.translate("construct_preview");

	private final CannonDataHolder data;

	public ConstructPreviewWidget(int size, CannonDataHolder data) {
		super(0, 0, size, size, MESSAGE);
		this.data = data;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
//		graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xFFaaaaaa);
		// render shadow: todo

		// render construct
		this.data.get().construct().map(ConstructManager.INSTANCE::getConstructSet).ifPresent(set -> {
			PoseStack matrices = graphics.pose();
			matrices.pushPose();
			matrices.translate(this.getX(), this.getY(), 0);
			matrices.scale(40, 40, 1);
			set.getDefault().getBlocks(Rotation.NONE).forEach((pos, info) -> {
				matrices.pushPose();
				matrices.translate(pos.getX(), pos.getY(), pos.getZ());
				matrices.mulPose(Axis.YN.rotationDegrees(Minecraft.getInstance().player.tickCount));
				Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
						info.state(), matrices, graphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY
				);
				matrices.popPose();
			});
			matrices.popPose();
		});
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		builder.add(NarratedElementType.TITLE, MESSAGE);
	}
}
