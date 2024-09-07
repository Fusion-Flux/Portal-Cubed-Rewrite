package io.github.fusionflux.portalcubed.content.prop;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.framework.model.TransformingBakedModel;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.Items;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;

import java.util.function.Function;

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

	private static final class ModelEmitter implements VertexConsumer, VertexBufferWriter {
		private static final RenderType EMISSIVE_RENDER_TYPE = RenderType.beaconBeam(TextureAtlas.LOCATION_BLOCKS, true);
		private static final RenderType DEFAULT_RENDER_TYPE = Sheets.translucentItemSheet();
		private static final RenderType CUTOUT_RENDER_TYPE = Sheets.cutoutBlockSheet();

		// Mimics indigo's logic
		private static RenderType getRenderType(RenderMaterial material) {
			BlendMode blendMode = material.blendMode();
			if (material.emissive()) {
				return EMISSIVE_RENDER_TYPE;
			} else if (blendMode == BlendMode.DEFAULT || blendMode == BlendMode.TRANSLUCENT) {
				return DEFAULT_RENDER_TYPE;
			} else {
				return CUTOUT_RENDER_TYPE;
			}
		}

		private Function<RenderType, VertexConsumer> vertexConsumers;
		private VertexConsumer delegate;
		private final WrapperModel model = new WrapperModel();

		private void prepare(Function<RenderType, VertexConsumer> vertexConsumers, BakedModel model) {
			this.model.setWrappedModel(model);
			this.vertexConsumers = vertexConsumers;
		}

		private void cleanup() {
			this.model.setWrappedModel(null);
			this.vertexConsumers = null;
			this.delegate = null;
		}

		@Override
		@NotNull
		public VertexConsumer vertex(double x, double y, double z) {
			this.delegate.vertex(x, y, z);
			return this;
		}

		@Override
		@NotNull
		public VertexConsumer color(int red, int green, int blue, int alpha) {
			this.delegate.color(red, green, blue, alpha);
			return this;
		}

		@Override
		@NotNull
		public VertexConsumer uv(float u, float v) {
			this.delegate.uv(u, v);
			return this;
		}

		@Override
		@NotNull
		public VertexConsumer overlayCoords(int u, int v) {
			this.delegate.overlayCoords(u, v);
			return this;
		}

		@Override
		@NotNull
		public VertexConsumer uv2(int u, int v) {
			this.delegate.uv2(u, v);
			return this;
		}

		@Override
		@NotNull
		public VertexConsumer normal(float x, float y, float z) {
			this.delegate.normal(x, y, z);
			return this;
		}

		@Override
		public void endVertex() {
			this.delegate.endVertex();
		}

		@Override
		public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
			this.delegate.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
		}

		@Override
		public void defaultColor(int red, int green, int blue, int alpha) {
			this.delegate.defaultColor(red, green, blue, alpha);
		}

		@Override
		public void unsetDefaultColor() {
			this.delegate.unsetDefaultColor();
		}

		@Override
		public void putBulkData(PoseStack.Pose matrixEntry, BakedQuad quad, float red, float green, float blue, int light, int overlay) {
			this.delegate.putBulkData(matrixEntry, quad, red, green, blue, light, overlay);
		}

		@Override
		public void putBulkData(PoseStack.Pose matrixEntry, BakedQuad quad, float[] brightnesses, float red, float green, float blue, int[] lights, int overlay, boolean useQuadColorData) {
			this.delegate.putBulkData(matrixEntry, quad, brightnesses, red, green, blue, lights, overlay, useQuadColorData);
		}

		@Override
		public void push(MemoryStack stack, long ptr, int count, VertexFormatDescription format) {
			VertexBufferWriter.of(this.delegate).push(stack, ptr, count, format);
		}

		@Override
		public boolean canUseIntrinsics() {
			return VertexBufferWriter.tryOf(this.delegate) != null;
		}

		private final class WrapperModel extends TransformingBakedModel {
			private WrapperModel() {
				super((quad -> {
					ModelEmitter.this.delegate = ModelEmitter.this.vertexConsumers.apply(getRenderType(quad.material()));
					return true;
				}));
			}

			private void setWrappedModel(BakedModel wrapped) {
				this.wrapped = wrapped;
			}
		}
	}
}
