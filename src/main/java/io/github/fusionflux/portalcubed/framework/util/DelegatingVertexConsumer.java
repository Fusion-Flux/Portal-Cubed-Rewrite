package io.github.fusionflux.portalcubed.framework.util;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.minecraft.client.renderer.block.model.BakedQuad;

public class DelegatingVertexConsumer implements VertexConsumer, VertexBufferWriter {
	protected VertexConsumer delegate;

	@Override
	@NotNull
	public VertexConsumer addVertex(float x, float y, float z) {
		this.delegate.addVertex(x, y, z);
		return this;
	}

	@Override
	@NotNull
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		this.delegate.setColor(red, green, blue, alpha);
		return this;
	}

	@Override
	@NotNull
	public VertexConsumer setUv(float u, float v) {
		this.delegate.setUv(u, v);
		return this;
	}

	@Override
	@NotNull
	public VertexConsumer setUv1(int u, int v) {
		this.delegate.setUv1(u, v);
		return this;
	}

	@Override
	@NotNull
	public VertexConsumer setUv2(int u, int v) {
		this.delegate.setUv2(u, v);
		return this;
	}

	@Override
	@NotNull
	public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
		this.delegate.setNormal(normalX, normalY, normalZ);
		return this;
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
		this.delegate.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay, boolean readAlpha) {
		this.delegate.putBulkData(pose, quad, brightness, red, green, blue, alpha, lightmap, packedOverlay, readAlpha);
	}

	@Override
	public void push(MemoryStack stack, long ptr, int count, VertexFormat format) {
		VertexBufferWriter.of(this.delegate).push(stack, ptr, count, format);
	}

	@Override
	public boolean canUseIntrinsics() {
		return VertexBufferWriter.tryOf(this.delegate) != null;
	}
}
