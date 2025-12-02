package io.github.fusionflux.portalcubed.framework.util;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3dc;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import io.github.fusionflux.portalcubed.framework.extension.Vec3Ext;
import io.github.fusionflux.portalcubed.framework.shape.Line;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.framework.shape.Plane;
import io.github.fusionflux.portalcubed.framework.shape.Quad;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RenderingUtils {
	private static final Matrix4f MATRIX = new Matrix4f();
	private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();
	private static VertexBuffer fullscreenQuadBuffer = null;

	// mostly yoinked from DragonFireballRenderer
	public static void renderQuad(PoseStack matrices, VertexConsumer vertices, int light, int color) {
		PoseStack.Pose pose = matrices.last();
		quadVertex(vertices, pose, light, 1, 1, color, 1, 1);
		quadVertex(vertices, pose, light, 1, 0, color, 1, 0);
		quadVertex(vertices, pose, light, 0, 0, color, 0, 0);
		quadVertex(vertices, pose, light, 0, 1, color, 0, 1);
	}

	public static void quadVertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, int light, float x, int z, int color, float textureU, float textureV) {
		vertexConsumer.addVertex(pose, x, 0, z)
				.setColor(color)
				.setUv(textureU, textureV)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(light)
				.setNormal(0, 1, 0);
	}

	public static void renderShape(PoseStack matrices, MultiBufferSource vertexConsumers, VoxelShape shape, Color color) {
		// LevelRenderer method just renders the aabbs
		for (AABB aabb : shape.toAabbs()) {
			renderBox(matrices, vertexConsumers, aabb, color);
		}
	}

	public static void renderVec(PoseStack matrices, MultiBufferSource vertexConsumers, Vec3 vec, Vec3 pos, Color color) {
		Vec3 to = pos.add(vec);
		renderLine(matrices, vertexConsumers, pos, to, color);
	}

	public static void renderPos(PoseStack matrices, MultiBufferSource buffers, Vec3 pos, float size, Color color) {
		AABB box = AABB.ofSize(pos, size, size, size);
		renderBox(matrices, buffers, box, color);
	}

	public static void renderBox(PoseStack matrices, MultiBufferSource vertexConsumers, AABB box, Color color) {
		VertexConsumer vertices = vertexConsumers.getBuffer(RenderType.lines());
		ShapeRenderer.renderLineBox(matrices, vertices, box, color.r(), color.g(), color.b(), color.a());
	}

	public static void renderBox(PoseStack matrices, MultiBufferSource vertexConsumers, OBB box, Color color) {
		matrices.pushPose();
		matrices.translate(box.center.x(), box.center.y(), box.center.z());
		matrices.mulPose(box.rotation.getUnnormalizedRotation(new Quaternionf()));
		renderBox(matrices, vertexConsumers, AABB.ofSize(Vec3.ZERO, box.extents.x() * 2, box.extents.y() * 2, box.extents.z() * 2), color);
		matrices.popPose();

		for (Vector3dc vertex : box.vertices()) {
			renderPos(matrices, vertexConsumers, Vec3Ext.of(vertex), 0.05f, color);
		}
	}

	public static void renderPlane(PoseStack matrices, MultiBufferSource vertexConsumers, Plane plane, float size, Color color) {
		Quad quad = Quad.create(plane, size);
		renderQuad(matrices, vertexConsumers, quad, color);
		Vec3 normal = plane.normal();
		renderVec(matrices, vertexConsumers, normal, Vec3Ext.of(quad.bottomLeft()), color);
		renderVec(matrices, vertexConsumers, normal, Vec3Ext.of(quad.bottomRight()), color);
		renderVec(matrices, vertexConsumers, normal, Vec3Ext.of(quad.topRight()), color);
		renderVec(matrices, vertexConsumers, normal, Vec3Ext.of(quad.topLeft()), color);
	}

	public static void renderQuad(PoseStack matrices, MultiBufferSource vertexConsumers, Quad quad, Color color) {
		Vec3 bottomLeft = Vec3Ext.of(quad.bottomLeft());
		Vec3 bottomRight = Vec3Ext.of(quad.bottomRight());
		Vec3 topRight = Vec3Ext.of(quad.topRight());
		Vec3 topLeft = Vec3Ext.of(quad.topLeft());

		renderLine(matrices, vertexConsumers, bottomLeft, bottomRight, color);
		renderLine(matrices, vertexConsumers, bottomRight, topRight, color);
		renderLine(matrices, vertexConsumers, topRight, topLeft, color);
		renderLine(matrices, vertexConsumers, topLeft, bottomLeft, color);
	}

	public static void renderLine(PoseStack matrices, MultiBufferSource buffers, Line line, Color color) {
		renderLine(matrices, buffers, line.from(), line.to(), color);
	}

	public static void renderLine(PoseStack matrices, MultiBufferSource vertexConsumers, Vec3 from, Vec3 to, Color color) {
		VertexConsumer vertices = vertexConsumers.getBuffer(RenderType.lines());
		PoseStack.Pose pose = matrices.last();
		Vec3 normal = to.subtract(from).normalize();
		vertices.addVertex(pose, (float) from.x, (float) from.y, (float) from.z)
				.setColor(color.r(), color.g(), color.b(), color.a())
				.setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
		vertices.addVertex(pose, (float) to.x, (float) to.y, (float) to.z)
				.setColor(color.r(), color.g(), color.b(), color.a())
				.setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
	}

	public static void drawGuiManaged(Runnable runnable) {
		RenderSystem.disableDepthTest();
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(RenderSystem.getProjectionMatrix().translate(0, 0, -11000, MATRIX), RenderSystem.getProjectionType());
		runnable.run();
		RenderSystem.restoreProjectionMatrix();
		RenderSystem.enableDepthTest();
	}

	public static void setupStencilToRenderIfValue(int value) {
		RenderSystem.stencilFunc(GL11.GL_EQUAL, value, 0xFF);
	}

	public static void setupStencilForWriting(int value, boolean increase) {
		setupStencilToRenderIfValue(value);
		RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, increase ? GL11.GL_INCR : GL11.GL_DECR);
		RenderSystem.stencilMask(0xFF);
	}

	public static void defaultStencil() {
		// Values gotten from GlStateManager.Stencil*
		RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
		RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
		RenderSystem.stencilMask(0xFF);
	}

	public static void renderFullScreenQuad(float red, float green, float blue) {
		if (fullscreenQuadBuffer == null) {
			try (ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(DefaultVertexFormat.POSITION_COLOR.getVertexSize() * 4)) {
				BufferBuilder builder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
				builder.addVertex(-1, -1, 0).setColor(-1);
				builder.addVertex(1, -1, 0).setColor(-1);
				builder.addVertex(1, 1, 0).setColor(-1);
				builder.addVertex(-1, 1, 0).setColor(-1);
				fullscreenQuadBuffer = new VertexBuffer(BufferUsage.STATIC_WRITE);
				fullscreenQuadBuffer.bind();
				fullscreenQuadBuffer.upload(builder.buildOrThrow());
				VertexBuffer.unbind();
			}
		}

		// Setup state
		RenderSystem.depthFunc(GL11.GL_ALWAYS);
		RenderSystem.setShaderColor(red, green, blue, 1f);

		fullscreenQuadBuffer.bind();
		fullscreenQuadBuffer.drawWithShader(IDENTITY_MATRIX, IDENTITY_MATRIX, RenderSystem.setShader(CoreShaders.POSITION_COLOR));
		VertexBuffer.unbind();

		// Cleanup state
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}

	public static void renderMesh(MeshData mesh, RenderType renderType, ByteBufferBuilder byteBufferBuilder) {
		if (renderType.sortOnUpload()) {
			mesh.sortQuads(byteBufferBuilder, RenderSystem.getProjectionType().vertexSorting());
		}
		renderType.draw(mesh);
	}
}
