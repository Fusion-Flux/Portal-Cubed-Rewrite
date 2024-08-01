package io.github.fusionflux.portalcubed.content.prop;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.Items;

import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PropRenderer extends EntityRenderer<Prop> {
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

		WrapperModel model = WrapperModel.INSTANCE.setup(vertexConsumers, PropModelCache.INSTANCE.get(prop));
		matrices.pushPose();
		matrices.mulPose(Axis.YP.rotationDegrees(180 - prop.getYRot()));
		matrices.translate(0, Y_OFFSET, 0);
		matrices.scale(2, 2, 2);
		this.itemRenderer.render(NON_EMPTY_STACK, ItemDisplayContext.GROUND, false, matrices, model, light, OverlayTexture.NO_OVERLAY, model);
		matrices.popPose();
	}

	@Override
	@SuppressWarnings("deprecation")
	@NotNull
	public ResourceLocation getTextureLocation(Prop entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	private static final class WrapperModel extends ForwardingBakedModel implements MultiBufferSource, VertexConsumer {
		private static final WrapperModel INSTANCE = new WrapperModel();

		private static final RenderType EMISSIVE_RENDER_TYPE = RenderType.beaconBeam(TextureAtlas.LOCATION_BLOCKS, true);
		private static final RenderType DEFAULT_RENDER_TYPE = Sheets.translucentItemSheet();
		private static final RenderType CUTOUT_RENDER_TYPE = Sheets.cutoutBlockSheet();

		// Mimics indigo's logic
		private static RenderType selectRenderType(RenderMaterial material) {
			BlendMode blendMode = material.blendMode();
			if (material.emissive()) {
				return EMISSIVE_RENDER_TYPE;
			} else if (blendMode == BlendMode.DEFAULT || blendMode == BlendMode.TRANSLUCENT) {
				return DEFAULT_RENDER_TYPE;
			} else {
				return CUTOUT_RENDER_TYPE;
			}
		}

		private MultiBufferSource wrappedBufferSource;
		private VertexConsumer overridingVertexConsumer;
		private final RenderContext.QuadTransform quadTransform = quad -> {
			this.overridingVertexConsumer = this.wrappedBufferSource.getBuffer(selectRenderType(quad.material()));
			return true;
		};

		private WrapperModel setup(MultiBufferSource bufferSource, BakedModel model) {
			this.wrappedBufferSource = bufferSource;
			this.wrapped = model;
			return this;
		}

		@Override
		public boolean isVanillaAdapter() {
			return false;
		}

		@Override
		public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
			context.pushTransform(this.quadTransform);
			super.emitItemQuads(stack, randomSupplier, context);
			context.popTransform();
		}

		@Override
		public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
			throw new UnsupportedOperationException("If you are seeing this, something is very wrong");
		}

		@Override
		@NotNull
		public VertexConsumer getBuffer(RenderType renderType) {
			return this;
		}

		@Override
		@NotNull
		public VertexConsumer vertex(double x, double y, double z) {
			this.overridingVertexConsumer.vertex(x, y, z);
			return this;
		}

		@Override
		@NotNull
		public VertexConsumer color(int red, int green, int blue, int alpha) {
			this.overridingVertexConsumer.color(red, green, blue, alpha);
			return this;
		}

		@Override
		@NotNull
		public VertexConsumer uv(float u, float v) {
			this.overridingVertexConsumer.uv(u, v);
			return this;
		}

		@Override
		@NotNull
		public VertexConsumer overlayCoords(int u, int v) {
			this.overridingVertexConsumer.overlayCoords(u, v);
			return this;
		}

		@Override
		@NotNull
		public VertexConsumer uv2(int u, int v) {
			this.overridingVertexConsumer.uv2(u, v);
			return this;
		}

		@Override
		@NotNull
		public VertexConsumer normal(float x, float y, float z) {
			this.overridingVertexConsumer.normal(x, y, z);
			return this;
		}

		@Override
		public void endVertex() {
			this.overridingVertexConsumer.endVertex();
		}

		@Override
		public void defaultColor(int red, int green, int blue, int alpha) {
			this.overridingVertexConsumer.defaultColor(red, green, blue, alpha);
		}

		@Override
		public void unsetDefaultColor() {
			this.overridingVertexConsumer.unsetDefaultColor();
		}
	}
}
