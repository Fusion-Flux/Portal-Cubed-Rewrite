package io.github.fusionflux.portalcubed.framework.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

import net.minecraft.world.phys.Vec3;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RenderingUtils {
	// mostly yoinked from DragonFireballRenderer
	public static void renderQuad(PoseStack matrices, VertexConsumer vertices, int light, int color) {
		PoseStack.Pose pose = matrices.last();
		Matrix4f matrix4f = pose.pose();
		Matrix3f matrix3f = pose.normal();
		vertex(vertices, matrix4f, matrix3f, light, 1, 1, color, 1, 1);
		vertex(vertices, matrix4f, matrix3f, light, 1, 0, color, 1, 0);
		vertex(vertices, matrix4f, matrix3f, light, 0, 0, color, 0, 0);
		vertex(vertices, matrix4f, matrix3f, light, 0, 1, color, 0, 1);
	}

	public static void renderLine(PoseStack matrices, MultiBufferSource vertexConsumers, Vec3 from, Vec3 to, Color color) {
		VertexConsumer vertices = vertexConsumers.getBuffer(RenderType.lines());
		PoseStack.Pose pose = matrices.last();
		Vec3 normal = to.subtract(from).normalize();
		vertices.vertex(pose.pose(), (float) from.x, (float) from.y, (float) from.z)
				.color(color.r(), color.g(), color.b(), color.a())
				.normal(pose.normal(), (float) normal.x, (float) normal.y, (float) normal.z)
				.endVertex();
		vertices.vertex(pose.pose(), (float) to.x, (float) to.y, (float) to.z)
				.color(color.r(), color.g(), color.b(), color.a())
				.normal(pose.normal(), (float) normal.x, (float) normal.y, (float) normal.z)
				.endVertex();
	}

	private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix, Matrix3f normalMatrix, int light, float x, int y, int color, int textureU, int textureV) {
		vertexConsumer.vertex(matrix, x, y, 0)
				.color(color)
				.uv(textureU, textureV)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(light)
				.normal(normalMatrix, 0, 1, 0)
				.endVertex();
	}
}
