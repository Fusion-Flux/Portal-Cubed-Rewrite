package io.github.fusionflux.portalcubed.content.portal.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.util.RenderingUtil;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class PortalRenderer extends EntityRenderer<Portal> {
	public static final ResourceLocation ROUND_TEXTURE = PortalCubed.id("textures/entity/portal/round.png");
	public static final ResourceLocation SQUARE_TEXTURE = PortalCubed.id("textures/entity/portal/square.png");
	public static final ResourceLocation ROUND_TRACER = PortalCubed.id("textures/entity/portal/tracer/round.png");
	public static final ResourceLocation SQUARE_TRACER = PortalCubed.id("textures/entity/portal/tracer/square.png");

	public PortalRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(Portal portal, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
		super.render(portal, yaw, tickDelta, matrices, vertexConsumers, light);
		VertexConsumer vertices = vertexConsumers.getBuffer(RenderType.beaconBeam(getTextureLocation(portal), true));
		matrices.pushPose();
		matrices.mulPose(Axis.YP.rotationDegrees(portal.getYRot()));
		matrices.mulPose(Axis.XP.rotationDegrees(portal.getXRot()));
		matrices.translate(-0.5f, -1, 0);
		matrices.scale(2, 2, 2);
		RenderingUtil.renderQuad(matrices, vertices, LightTexture.FULL_BRIGHT, portal.getColor());
		matrices.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(Portal entity) {
		return switch (entity.getPortalShape()) {
			case ROUND -> ROUND_TEXTURE;
			case SQUARE -> SQUARE_TEXTURE;
		};
	}
}
