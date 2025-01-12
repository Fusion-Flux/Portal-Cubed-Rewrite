package io.github.fusionflux.portalcubed.content.prop.renderer;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.framework.model.TransformingBakedModel;
import io.github.fusionflux.portalcubed.framework.util.DelegatingVertexConsumer;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;

public class PropRenderer extends EntityRenderer<Prop, PropRenderState> {
	private static final ModelEmitter EMITTER = new ModelEmitter();
	private static final int[] EMPTY_TINT_LAYERS = new int[0];
	private static final float Y_OFFSET = 2 / 16f;

	public PropRenderer(Context ctx) {
		super(ctx);
	}

	@Override
	public void render(PropRenderState renderState, PoseStack matrices, MultiBufferSource bufferSource, int light) {
		super.render(renderState, matrices, bufferSource, light);

		PropModelCache.ModelTransformPair modelTransformPair = PropModelCache.INSTANCE.get(renderState);
		EMITTER.prepare(bufferSource::getBuffer, modelTransformPair.model());
		matrices.pushPose();
		matrices.mulPose(Axis.YP.rotationDegrees(180 - renderState.yRot));
		matrices.translate(0, Y_OFFSET, 0);
		matrices.scale(2, 2, 2);
		modelTransformPair.applyTransform(matrices);
		ItemRenderer.renderItem(
				ItemDisplayContext.GROUND,
				matrices,
				renderType -> EMITTER,
				light,
				OverlayTexture.NO_OVERLAY,
				EMPTY_TINT_LAYERS,
				EMITTER.model,
				ModelEmitter.DEFAULT_RENDER_TYPE,
				ItemStackRenderState.FoilType.NONE
		);
		matrices.popPose();
		EMITTER.cleanup();
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

	private static final class ModelEmitter extends DelegatingVertexConsumer {
		@SuppressWarnings("deprecation")
		private static final RenderType EMISSIVE_RENDER_TYPE = RenderType.beaconBeam(TextureAtlas.LOCATION_BLOCKS, false);
		private static final RenderType DEFAULT_RENDER_TYPE = Sheets.translucentItemSheet();
		private static final RenderType CUTOUT_RENDER_TYPE = Sheets.cutoutBlockSheet();

		private Function<RenderType, VertexConsumer> bufferMapper;
		private final DelegateModel model = new DelegateModel();

		private void prepare(Function<RenderType, VertexConsumer> bufferMapper, BakedModel model) {
			this.model.setDelegate(model);
			this.bufferMapper = bufferMapper;
		}

		private void cleanup() {
			this.model.setDelegate(null);
			this.bufferMapper = null;
			this.delegate = null;
		}

		private void prepareForMaterial(RenderMaterial material) {
			BlendMode blendMode = material.blendMode();
			RenderType renderType;
			if (material.emissive()) {
				renderType = EMISSIVE_RENDER_TYPE;
			} else if (blendMode == BlendMode.DEFAULT || blendMode == BlendMode.TRANSLUCENT) {
				renderType = DEFAULT_RENDER_TYPE;
			} else {
				renderType = CUTOUT_RENDER_TYPE;
			}
			this.delegate = this.bufferMapper.apply(renderType);
		}

		private final class DelegateModel extends TransformingBakedModel {
			private DelegateModel() {
				super((quad -> {
					ModelEmitter.this.prepareForMaterial(quad.material());
					return true;
				}));
			}

			private void setDelegate(BakedModel delegate) {
				this.delegate = delegate;
			}
		}
	}
}
