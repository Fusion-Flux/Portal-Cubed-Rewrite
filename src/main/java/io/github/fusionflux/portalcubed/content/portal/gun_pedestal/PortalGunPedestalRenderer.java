package io.github.fusionflux.portalcubed.content.portal.gun_pedestal;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.model.BlockEntityWithModelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;

public class PortalGunPedestalRenderer extends BlockEntityWithModelRenderer<PortalGunPedestalBlockEntity, PortalGunPedestalModel> {
	public static final ResourceLocation TEXTURE = PortalCubed.id("textures/entity/portal_gun_pedestal/portal_gun_pedestal.png");

	private final ItemRenderer itemRenderer;

	public PortalGunPedestalRenderer(BlockEntityRendererProvider.Context ctx) {
		super(new PortalGunPedestalModel(ctx.bakeLayer(PortalGunPedestalModel.LAYER_LOCATION)));
		this.itemRenderer = ctx.getItemRenderer();
	}

	@Override
	public void render(PortalGunPedestalBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
		matrices.translate(0f, 1f, 0f);
		super.render(entity, tickDelta, matrices, vertexConsumers, light, overlay);
	}

	@Override
	public ResourceLocation getTextureLocation(PortalGunPedestalBlockEntity entity) {
		return TEXTURE;
	}
}
