package io.github.fusionflux.portalcubed.framework.model;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class BlockEntityWithModelRenderer<T extends BlockEntity, M extends BlockEntityModel<T>> implements BlockEntityRenderer<T> {
	public final M model;

	protected BlockEntityWithModelRenderer(M model) {
		this.model = model;
	}

	@Override
	public void render(T entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
		matrices.translate(.5f, .5f, .5f);
		matrices.pushPose();
		matrices.scale(-1f, -1f, 1f);
		this.model.setupAnim(entity, tickDelta);
		RenderType renderType = this.model.renderType(this.getTextureLocation(entity));
		this.model.renderToBuffer(matrices, vertexConsumers.getBuffer(renderType), LightTexture.FULL_BRIGHT, overlay, -1);
		matrices.popPose();
	}

	public abstract ResourceLocation getTextureLocation(T entity);
}
