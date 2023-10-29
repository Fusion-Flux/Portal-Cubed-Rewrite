package io.github.fusionflux.portalcubed.content.portal.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
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
		int color = portal.getColor();

		// mostly yoinked from DragonFireballRenderer
		PoseStack.Pose pose = matrices.last();
		Matrix4f matrix4f = pose.pose();
		Matrix3f matrix3f = pose.normal();
		matrices.pushPose();
		matrices.mulPose(portal.getRotation());
		vertex(vertices, matrix4f, matrix3f, light, 0, 0, color, 0, 1);
		vertex(vertices, matrix4f, matrix3f, light, 2, 0, color, 1, 1);
		vertex(vertices, matrix4f, matrix3f, light, 2, 2, color, 1, 0);
		vertex(vertices, matrix4f, matrix3f, light, 0, 2, color, 0, 0);
		matrices.popPose();
	}

	private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix, Matrix3f normalMatrix, int light, float x, int y, int color, int textureU, int textureV) {
		vertexConsumer.vertex(matrix, x - 0.5f, y, 0)
				.color(color | 0xFF000000)
				.uv(textureU, textureV)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(light)
				.normal(normalMatrix, 0, 1, 0)
				.endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(Portal entity) {
		return switch (entity.getPortalShape()) {
			case ROUND -> ROUND_TEXTURE;
			case SQUARE -> SQUARE_TEXTURE;
		};
	}
}
