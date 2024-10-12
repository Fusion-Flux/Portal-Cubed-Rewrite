package io.github.fusionflux.portalcubed.framework.util;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.minecraft.client.renderer.block.model.BakedQuad;

public abstract class DelegatingVertexConsumer implements VertexConsumer, VertexBufferWriter {
	protected VertexConsumer delegate;

	@Override
	@NotNull
	public VertexConsumer vertex(double x, double y, double z) {
		this.delegate.vertex(x, y, z);
		return this;
	}

	@Override
	@NotNull
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		this.delegate.color(red, green, blue, alpha);
		return this;
	}

	@Override
	@NotNull
	public VertexConsumer uv(float u, float v) {
		this.delegate.uv(u, v);
		return this;
	}

	@Override
	@NotNull
	public VertexConsumer overlayCoords(int u, int v) {
		this.delegate.overlayCoords(u, v);
		return this;
	}

	@Override
	@NotNull
	public VertexConsumer uv2(int u, int v) {
		this.delegate.uv2(u, v);
		return this;
	}

	@Override
	@NotNull
	public VertexConsumer normal(float x, float y, float z) {
		this.delegate.normal(x, y, z);
		return this;
	}

	@Override
	public void endVertex() {
		this.delegate.endVertex();
	}

	@Override
	public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
		this.delegate.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
	}

	@Override
	public void defaultColor(int red, int green, int blue, int alpha) {
		this.delegate.defaultColor(red, green, blue, alpha);
	}

	@Override
	public void unsetDefaultColor() {
		this.delegate.unsetDefaultColor();
	}

	@Override
	public void putBulkData(PoseStack.Pose matrixEntry, BakedQuad quad, float red, float green, float blue, int light, int overlay) {
		this.delegate.putBulkData(matrixEntry, quad, red, green, blue, light, overlay);
	}

	@Override
	public void putBulkData(PoseStack.Pose matrixEntry, BakedQuad quad, float[] brightnesses, float red, float green, float blue, int[] lights, int overlay, boolean useQuadColorData) {
		this.delegate.putBulkData(matrixEntry, quad, brightnesses, red, green, blue, lights, overlay, useQuadColorData);
	}

	@Override
	public void push(MemoryStack stack, long ptr, int count, VertexFormatDescription format) {
		VertexBufferWriter.of(this.delegate).push(stack, ptr, count, format);
	}

	@Override
	public boolean canUseIntrinsics() {
		return VertexBufferWriter.tryOf(this.delegate) != null;
	}
}
