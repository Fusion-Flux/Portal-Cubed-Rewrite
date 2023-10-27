package io.github.fusionflux.portalcubed.content.portal.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import org.joml.Vector3f;

public class PortalRenderer extends EntityRenderer<Portal> {
	public static final ResourceLocation ROUND_TEXTURE = PortalCubed.id("textures/entity/portal/round.png");
	public static final ResourceLocation SQUARE_TEXTURE = PortalCubed.id("textures/entity/portal/square.png");
	public static final ResourceLocation ROUND_TRACER = PortalCubed.id("textures/entity/portal/tracer/round.png");
	public static final ResourceLocation SQUARE_TRACER = PortalCubed.id("textures/entity/portal/tracer/square.png");

	private static final Vector3f normal = new Vector3f(0, 0, 1);

	public PortalRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(Portal portal, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
		super.render(portal, yaw, tickDelta, matrices, vertexConsumers, light);
		VertexConsumer vertices = vertexConsumers.getBuffer(RenderType.entityCutout(getTextureLocation(portal)));
		vertices.vertex(
				1, 1, 0,
				1, 1, 1, 1,
				1, 1,
				OverlayTexture.NO_OVERLAY, light,
				normal.x, normal.y, normal.z
		);
		vertices.vertex(
				0, 1, 0,
				1, 1, 1, 1,
				0, 1,
				OverlayTexture.NO_OVERLAY, light,
				normal.x, normal.y, normal.z
		);
		vertices.vertex(
				0, 0, 0,
				1, 1, 1, 1,
				0, 0,
				OverlayTexture.NO_OVERLAY, light,
				normal.x, normal.y, normal.z
		);

//		vertices.vertex(
//				1, 0, 0,
//				1, 1, 1, 1,
//				1, 0,
//				OverlayTexture.NO_OVERLAY, light,
//				normal.x, normal.y, normal.z
//		);
	}

	@Override
	public ResourceLocation getTextureLocation(Portal entity) {
		return switch (entity.getPortalShape()) {
			case ROUND -> ROUND_TEXTURE;
			case SQUARE -> SQUARE_TEXTURE;
		};
	}
}
