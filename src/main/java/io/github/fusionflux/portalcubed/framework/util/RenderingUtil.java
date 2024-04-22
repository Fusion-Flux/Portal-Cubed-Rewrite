package io.github.fusionflux.portalcubed.framework.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.texture.OverlayTexture;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RenderingUtil {
	private static final Matrix4f MATRIX = new Matrix4f();

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

	private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix, Matrix3f normalMatrix, int light, float x, int y, int color, int textureU, int textureV) {
		vertexConsumer.vertex(matrix, x, 0, y)
				.color(color)
				.uv(textureU, textureV)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(light)
				.normal(normalMatrix, 0, 1, 0)
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
