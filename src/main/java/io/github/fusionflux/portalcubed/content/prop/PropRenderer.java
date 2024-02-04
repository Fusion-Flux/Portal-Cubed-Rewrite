package io.github.fusionflux.portalcubed.content.prop;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.mixin.client.ItemRendererAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PropRenderer extends EntityRenderer<PropEntity> {
	private final ItemRenderer itemRenderer;

	public PropRenderer(Context ctx) {
		super(ctx);
		this.itemRenderer = ctx.getItemRenderer();
	}

	@Override
	public void render(PropEntity prop, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
		super.render(prop, yaw, tickDelta, matrices, vertexConsumers, light);

		var model = PropModels.getModel(prop.type, prop.getVariant());
		matrices.pushPose();
		model.getTransforms().getTransform(ItemDisplayContext.GROUND).apply(false, matrices);
		matrices.scale(2, 2, 2);
		matrices.translate(-.5, -.1775, -.5);
		var consumer = vertexConsumers.getBuffer(Sheets.translucentItemSheet());
		if (model.isVanillaAdapter()) {
			((ItemRendererAccessor) itemRenderer).callRenderModelLists(model, ItemStack.EMPTY, light, OverlayTexture.NO_OVERLAY, matrices, consumer);
		} else {
			// TODO: Figure out FRAPI
		}
		matrices.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(PropEntity entity) {
		return null;
	}
}
