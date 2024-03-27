package io.github.fusionflux.portalcubed.content.cannon.screen.widget.construct;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public abstract class ConstructWidget extends AbstractWidget {
	public static final Vec3 ORIGIN = new Vec3(1, .5, 1);

	public ConstructWidget(int size, Component message) {
		super(0, 0, size, size, message);
	}

	@Nullable
	protected abstract ConfiguredConstruct getConstruct();

	protected void applyConstructTransformations(PoseStack matrices, float delta) {
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		ConfiguredConstruct preview = this.getConstruct();
		if (preview == null)
			return;
		PoseStack matrices = graphics.pose();
		matrices.pushPose();
		// translate to position in screen
		matrices.translate(this.getX(), this.getY(), 150);
		// scale to fit area
		// un-rotated, a block is 1 pixel.
		matrices.scale(this.getWidth(), this.getHeight(), 1);
//		// translate to center
//		Vec3 center = AABB.of(preview.bounds).getCenter();
//		matrices.translate(center.x, center.y, center.z);
		// scale so that no matter the orientation, it fits inside the area
		float sizeOnLargestAxis = Math.max(
				preview.bounds.getYSpan(),
				Math.max(preview.bounds.getXSpan(), preview.bounds.getZSpan())
		);
		float maxWidth = (float) Math.sqrt(2 * (sizeOnLargestAxis * sizeOnLargestAxis));
		matrices.scale(1 / maxWidth, 1 / maxWidth, 1);
		// currently perfectly fits in square. add some padding
//		matrices.scale(0.9f, 0.9f, 1);


		Vec3 renderOffset = Vec3.ZERO;//constructCenter.vectorTo(ORIGIN);
//		constructCenter = constructCenter.add(renderOffset);
		matrices.pushPose();
		prepareForBlockRendering(matrices);
//		matrices.translate(constructCenter.x, constructCenter.y, constructCenter.z);
		matrices.mulPose(Axis.XP.rotationDegrees(30));
		applyConstructTransformations(matrices, delta);
//		matrices.translate(-constructCenter.x, -constructCenter.y, -constructCenter.z);
		preview.blocks.forEach((pos, info) -> {
			matrices.pushPose();
			matrices.translate(pos.getX() + renderOffset.x, pos.getY() + renderOffset.y, pos.getZ() + renderOffset.z);
			Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
					info.state(), matrices, graphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY
			);
			matrices.popPose();
		});
		matrices.popPose();
		graphics.flush();
		matrices.popPose();
	}

	private void prepareForBlockRendering(PoseStack matrices) {
		matrices.mulPoseMatrix(new Matrix4f().scaling(1, -1, 1));
		RenderSystem.enableDepthTest();
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
