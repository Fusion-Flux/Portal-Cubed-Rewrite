package io.github.fusionflux.portalcubed.content.cannon.screen.widget.construct;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
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

	protected abstract void applyConstructTransformations(PoseStack matrices, float delta);

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		ConfiguredConstruct preview = this.getConstruct();
		if (preview == null)
			return;
		PoseStack matrices = graphics.pose();
		matrices.pushPose();
		matrices.translate(this.getX(), this.getY(), 150);
		matrices.scale(40, 40, 1);
		Vec3 constructCenter = AABB.of(preview.bounds).deflate(.5).getCenter();
		Vec3 renderOffset = constructCenter.vectorTo(ORIGIN);
		constructCenter = constructCenter.add(renderOffset);
		matrices.pushPose();
		prepareForBlockRendering(matrices);
		matrices.translate(constructCenter.x, constructCenter.y, constructCenter.z);
		applyConstructTransformations(matrices, delta);
		matrices.mulPose(Axis.XP.rotationDegrees(30));
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
	}

	private void prepareForBlockRendering(PoseStack matrices) {
		matrices.mulPoseMatrix(new Matrix4f().scaling(1, -1, 1));
		matrices.translate(0, -2, 0);
		RenderSystem.enableDepthTest();
	}
}
