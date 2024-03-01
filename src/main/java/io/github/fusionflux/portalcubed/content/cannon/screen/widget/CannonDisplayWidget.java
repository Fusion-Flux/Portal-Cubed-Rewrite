package io.github.fusionflux.portalcubed.content.cannon.screen.widget;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CannonDisplayWidget extends AbstractWidget {
	private static final ItemRenderer ITEM_RENDERER = Minecraft.getInstance().getItemRenderer();

	private final ItemStack item;
	private final BakedModel itemModel;
	private final float scale;

	public CannonDisplayWidget(int width, int height, ItemStack item) {
		super(0, 0, width, height, item.getDisplayName());
		this.item = item;
		this.itemModel = ITEM_RENDERER.getModel(item, Minecraft.getInstance().level, null, 0);
		this.scale = Math.min(width / 16f, height / 16f);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xFFaaaaaa);

		PoseStack matrices = graphics.pose();
		matrices.pushPose();

		matrices.scale(this.scale, this.scale, 1);
		matrices.translate(this.getX() / this.scale, this.getY() / this.scale, 0);
		renderCannon(graphics);
		matrices.popPose();
	}

	private void renderCannon(GuiGraphics graphics) {
		var matrices = graphics.pose();
		matrices.pushPose();

		matrices.translate(8, 8, 150);
		matrices.mulPoseMatrix(new Matrix4f().scaling(1, -1, 1));
		matrices.scale(16, 16, 16);
		ITEM_RENDERER.render(item, ItemDisplayContext.GUI, true, matrices, graphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, itemModel);
		graphics.flush();

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
