package io.github.fusionflux.portalcubed.framework.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RenderingUtils {
	private static final Matrix4f MATRIX = new Matrix4f();
	// mostly yoinked from DragonFireballRenderer
	public static void renderQuad(PoseStack matrices, VertexConsumer vertices, int light, int color) {
		PoseStack.Pose pose = matrices.last();
		Matrix4f matrix4f = pose.pose();
		Matrix3f matrix3f = pose.normal();
		quadVertex(vertices, matrix4f, matrix3f, light, 1, 1, color, 1, 1);
		quadVertex(vertices, matrix4f, matrix3f, light, 1, 0, color, 1, 0);
		quadVertex(vertices, matrix4f, matrix3f, light, 0, 0, color, 0, 0);
		quadVertex(vertices, matrix4f, matrix3f, light, 0, 1, color, 0, 1);
	}

	private static void quadVertex(VertexConsumer vertexConsumer, Matrix4f matrix, Matrix3f normalMatrix, int light, float x, int y, int color, int textureU, int textureV) {
		vertexConsumer.vertex(matrix, x, y, 0)
				.color(color)
				.uv(textureU, textureV)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(light)
				.normal(normalMatrix, 0, 1, 0)
				.endVertex();
	}

	public static void renderBox(PoseStack matrices, MultiBufferSource vertexConsumers, AABB box, Color color) {
		VertexConsumer vertices = vertexConsumers.getBuffer(RenderType.lines());
		LevelRenderer.renderLineBox(matrices, vertices, box, color.r(), color.g(), color.b(), color.a());
	}

	public static void renderQuad(PoseStack matrices, MultiBufferSource vertexConsumers, Quad quad, Color color) {
		renderTri(matrices, vertexConsumers, quad.a(), color);
		renderTri(matrices, vertexConsumers, quad.b(), color);
	}

	public static void renderTri(PoseStack matrices, MultiBufferSource buffers, Tri tri, Color color) {
		renderLine(matrices, buffers, tri.a(), tri.b(), color);
		renderLine(matrices, buffers, tri.a(), tri.c(), color);
		renderLine(matrices, buffers, tri.b(), tri.c(), color);
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

	public static void drawGuiManaged(Runnable runnable) {
		RenderSystem.disableDepthTest();
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(RenderSystem.getProjectionMatrix().translate(0, 0, -11000, MATRIX), RenderSystem.getVertexSorting());
		runnable.run();
		RenderSystem.restoreProjectionMatrix();
		RenderSystem.enableDepthTest();
	}
}
