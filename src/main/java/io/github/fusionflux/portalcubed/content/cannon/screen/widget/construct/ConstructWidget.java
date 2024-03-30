package io.github.fusionflux.portalcubed.content.cannon.screen.widget.construct;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructModelPool;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public abstract class ConstructWidget extends AbstractWidget {
	private final ConstructModelPool modelPool;

	public ConstructWidget(int size, Component message, ConstructModelPool modelPool) {
		super(0, 0, size, size, message);
		this.modelPool = modelPool;
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
		// align with slot
		matrices.translate(this.getWidth() / 2f, this.getHeight(), 0);
		// magic additional offset
		matrices.translate(0, 2, 0);

		// scale to fit area
		// un-rotated, a block is 1 pixel.
		matrices.scale(this.getWidth(), this.getHeight(), 1);
		// scale down slightly for padding
		matrices.scale(0.9f, 0.9f, 0.9f);

		if (preview.blocks.size() == 1) {
			// special case: further scale down single blocks, looks nicer
			matrices.scale(0.8f, 0.8f, 0.8f);
			// magic offset back to center
			matrices.translate(0, -0.14f, 0);
		}

		// scale so that no matter the orientation, it fits inside the area
		float sizeOnLargestAxis = Math.max(
				preview.bounds.getYSpan(),
				Math.max(preview.bounds.getXSpan(), preview.bounds.getZSpan())
		);
		float maxWidth = (float) Math.sqrt(2 * (sizeOnLargestAxis * sizeOnLargestAxis));
		matrices.scale(1 / maxWidth, 1 / maxWidth, 1);

		matrices.mulPoseMatrix(new Matrix4f().scaling(1, -1, 1));
		// tilt construct downwards, like items
		matrices.mulPose(Axis.XP.rotationDegrees(30));
		// apply custom transformations
		applyConstructTransformations(matrices, delta);

		// make the center the pivot point
		Vec3 center = AABB.of(preview.bounds).getCenter();
		matrices.translate(-center.x, center.y, -center.z);
		modelPool.getOrBuildModel(preview).render(matrices, graphics.bufferSource());
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
