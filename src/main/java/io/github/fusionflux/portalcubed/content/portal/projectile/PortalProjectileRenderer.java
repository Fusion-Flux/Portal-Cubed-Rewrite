package io.github.fusionflux.portalcubed.content.portal.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PortalProjectileRenderer extends EntityRenderer<PortalProjectile> {
	public static final ResourceLocation TEXTURE = new ResourceLocation("textures/block/magenta_glazed_terracotta.png");

	public PortalProjectileRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(PortalProjectile entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
		VertexConsumer vertices = vertexConsumers.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));

		matrices.pushPose();
		matrices.mulPose(entityRenderDispatcher.cameraOrientation());
		matrices.mulPose(Axis.YP.rotationDegrees(180));
		matrices.mulPose(Axis.XP.rotationDegrees(90));
		RenderingUtils.renderQuad(matrices, vertices, light, entity.getColor());
		matrices.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(PortalProjectile entity) {
		return TEXTURE;
	}
}
