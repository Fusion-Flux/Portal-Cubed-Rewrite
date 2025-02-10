package io.github.fusionflux.portalcubed.content.portal.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.framework.util.RenderingUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PortalProjectileRenderer extends EntityRenderer<PortalProjectile, PortalProjectileRenderState> {
	public static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/magenta_glazed_terracotta.png");

	public PortalProjectileRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(PortalProjectileRenderState renderState, PoseStack matrices, MultiBufferSource bufferSource, int light) {
		super.render(renderState, matrices, bufferSource, light);
		matrices.pushPose();
		matrices.translate(0, renderState.boundingBoxHeight / 2, 0);
		matrices.mulPose(this.entityRenderDispatcher.cameraOrientation());
		matrices.mulPose(Axis.YP.rotationDegrees(180));
		matrices.translate(-.5, -.5, 0);
		RenderingUtils.renderQuad(matrices, bufferSource.getBuffer(RenderType.beaconBeam(TEXTURE, true)), light, renderState.color);
		matrices.popPose();
	}

	@Override
	public PortalProjectileRenderState createRenderState() {
		return new PortalProjectileRenderState();
	}

	@Override
	public void extractRenderState(PortalProjectile projectile, PortalProjectileRenderState reusedState, float tickDelta) {
		super.extractRenderState(projectile, reusedState, tickDelta);
		reusedState.color = projectile.getColor();
	}
}
