package io.github.fusionflux.portalcubed.content.prop;

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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PropRenderer extends EntityRenderer<Prop> {
	private static final ModelEmitter EMITTER = new ModelEmitter();
	private static final ItemStack NON_EMPTY_STACK = new ItemStack(Items.BARRIER);
	private static final float Y_OFFSET = 2 / 16f;

	private final ItemRenderer itemRenderer;

	public PropRenderer(Context ctx) {
		super(ctx);
		this.itemRenderer = ctx.getItemRenderer();
	}

	@Override
	public void render(Prop prop, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
		super.render(prop, yaw, tickDelta, matrices, vertexConsumers, light);

		EMITTER.prepare(vertexConsumers::getBuffer, PropModelCache.INSTANCE.get(prop));
		matrices.pushPose();
		matrices.mulPose(Axis.YP.rotationDegrees(180 - prop.getYRot()));
		matrices.translate(0, Y_OFFSET, 0);
		matrices.scale(2, 2, 2);
		this.itemRenderer.render(NON_EMPTY_STACK, ItemDisplayContext.GROUND, false, matrices, renderType -> EMITTER, light, OverlayTexture.NO_OVERLAY, EMITTER.model);
		matrices.popPose();
		EMITTER.cleanup();
	}

	@Override
	@SuppressWarnings("deprecation")
	@NotNull
	public ResourceLocation getTextureLocation(Prop entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	private static final class ModelEmitter extends DelegatingVertexConsumer {
		@SuppressWarnings("deprecation")
		private static final RenderType EMISSIVE_RENDER_TYPE = RenderType.beaconBeam(TextureAtlas.LOCATION_BLOCKS, true);
		private static final RenderType DEFAULT_RENDER_TYPE = Sheets.translucentItemSheet();
		private static final RenderType CUTOUT_RENDER_TYPE = Sheets.cutoutBlockSheet();

		private Function<RenderType, VertexConsumer> bufferMapper;
		private final WrapperModel model = new WrapperModel();

		private void prepare(Function<RenderType, VertexConsumer> bufferMapper, BakedModel model) {
			this.model.setWrappedModel(model);
			this.bufferMapper = bufferMapper;
		}

		private void cleanup() {
			this.model.setWrappedModel(null);
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

		private final class WrapperModel extends TransformingBakedModel {
			private WrapperModel() {
				super((quad -> {
					ModelEmitter.this.prepareForMaterial(quad.material());
					return true;
				}));
			}

			private void setWrappedModel(BakedModel wrapped) {
				this.wrapped = wrapped;
			}
		}
	}
}
