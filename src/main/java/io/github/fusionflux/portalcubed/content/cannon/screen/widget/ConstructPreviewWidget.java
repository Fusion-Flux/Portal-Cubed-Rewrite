package io.github.fusionflux.portalcubed.content.cannon.screen.widget;

import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ConstructPreviewWidget extends AbstractWidget {
	public static final Component MESSAGE = ConstructionCannonScreen.translate("construct_preview");
	public static final Vec3 ORIGIN = new Vec3(1, .5, 1);

	private final CannonDataHolder data;

	private float animRot = 0;

	public ConstructPreviewWidget(int size, CannonDataHolder data) {
		super(0, 0, size, size, MESSAGE);
		this.data = data;
	}

	private void prepareForBlockRendering(PoseStack matrices) {
		matrices.mulPoseMatrix(new Matrix4f().scaling(1, -1, 1));
		matrices.translate(0, -2, 0);
		RenderSystem.enableDepthTest();
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
//		graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xFFaaaaaa);
		// render shadow: todo

		// render construct
		this.data.get().construct().map(ConstructManager.INSTANCE::getConstructSet).ifPresent(set -> {
			ConfiguredConstruct preview = set.preview;
			PoseStack matrices = graphics.pose();
			matrices.pushPose();
			matrices.translate(this.getX(), this.getY(), 0);
			matrices.scale(40, 40, 1);
			Vec3 constructCenter = AABB.of(preview.bounds).deflate(.5).getCenter();
			Vec3 renderOffset = constructCenter.vectorTo(ORIGIN);
			constructCenter = constructCenter.add(renderOffset);
			matrices.pushPose();
			prepareForBlockRendering(matrices);
			matrices.translate(constructCenter.x, constructCenter.y, constructCenter.z);
			matrices.mulPose(Axis.XP.rotationDegrees(30));
			matrices.mulPose(Axis.YP.rotationDegrees(this.animRot));
			this.animRot = (this.animRot + (delta * 2f)) % 360f;
			matrices.translate(-constructCenter.x, -constructCenter.y, -constructCenter.z);
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
		});
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		builder.add(NarratedElementType.TITLE, MESSAGE);
	}
}
