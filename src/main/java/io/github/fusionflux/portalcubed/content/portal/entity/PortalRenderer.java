package io.github.fusionflux.portalcubed.content.portal.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PortalRenderer extends EntityRenderer<Portal> {
	public static final ResourceLocation TEXTURE = PortalCubed.id("textures/entity/portal/portal.png");

	protected PortalRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(Portal portal, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
		super.render(portal, yaw, tickDelta, matrices, vertexConsumers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(Portal entity) {
		return TEXTURE;
	}
}
