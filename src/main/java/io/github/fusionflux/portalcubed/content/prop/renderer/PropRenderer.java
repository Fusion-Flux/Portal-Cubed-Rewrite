package io.github.fusionflux.portalcubed.content.prop.renderer;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

public class PropRenderer extends EntityRenderer<Prop, PropRenderState> {
	private static final int[] EMPTY_TINT_LAYERS = new int[0];
	private static final float Y_OFFSET = 2 / 16f;

	public PropRenderer(Context ctx) {
		super(ctx);
	}

	@Override
	public void submit(PropRenderState state, PoseStack matrices, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		super.submit(state, matrices, submitNodeCollector, camera);

		matrices.pushPose();
		matrices.rotate(Axis.YP.rotationDegrees(180 - state.yRot));
		matrices.translate(0, Y_OFFSET, 0);
		matrices.scale(2, 2, 2);
		PropModelCache.MeshAndTransform[] layers = PropModelCache.INSTANCE.get(state);
		for (PropModelCache.MeshAndTransform layer : layers) {
			matrices.pushPose();
			layer.applyTransform(matrices.last());
			submitNodeCollector.submitItem(
					matrices,
					ItemDisplayContext.GROUND,
					state.lightCoords,
					OverlayTexture.NO_OVERLAY,
					0,
					EMPTY_TINT_LAYERS,
					layer.quads(),
					ItemStackRenderState.FoilType.NONE

			);
			matrices.popPose();
		}
		matrices.popPose();
	}

	@Override
	@NotNull
	public PropRenderState createRenderState() {
		return new PropRenderState();
	}

	@Override
	public void extractRenderState(Prop prop, PropRenderState reusedState, float tickDelta) {
		super.extractRenderState(prop, reusedState, tickDelta);
		reusedState.type = prop.type;
		reusedState.variant = prop.getVariant();
		reusedState.yRot = prop.getYRot(tickDelta);
	}
}
