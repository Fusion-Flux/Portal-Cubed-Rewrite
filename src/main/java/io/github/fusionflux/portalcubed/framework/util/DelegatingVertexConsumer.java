package io.github.fusionflux.portalcubed.framework.util;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class DelegatingVertexConsumer implements VertexConsumer {
	protected VertexConsumer delegate;

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		this.delegate.addVertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		this.delegate.setColor(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexConsumer setColor(int color) {
		this.delegate.setColor(color);
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		this.delegate.setUv(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		this.delegate.setUv1(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		this.delegate.setUv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv3(float u, float v) {
		this.delegate.setUv3(u, v);
		return this;
	}

	@Override
	public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
		this.delegate.setNormal(normalX, normalY, normalZ);
		return this;
	}

	@Override
	public VertexConsumer setLineWidth(float width) {
		this.delegate.setLineWidth(width);
		return this;
	}
}
