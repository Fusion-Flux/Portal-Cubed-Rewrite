package io.github.fusionflux.portalcubed.content.fizzler;

import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.fusionflux.portalcubed.framework.extension.EntityExt;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorMixer;
import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;

public class DisintegrationVertexConsumer implements VertexConsumer, VertexBufferWriter {
	private static final float DARKEN = 0.15f;
	private static final float TRANSLUCENCY_START_PROGRESS = (EntityExt.DISINTEGRATE_TICKS - EntityExt.TRANSLUCENCY_START_TICKS) / (float) EntityExt.DISINTEGRATE_TICKS;

	private final VertexConsumer delegate;
	private final boolean canUseIntrinsics;

	private final int packedColor;
	private final float delta;

	public DisintegrationVertexConsumer(VertexConsumer delegate, float ticks) {
		this.delegate = delegate;
		this.canUseIntrinsics = VertexBufferWriter.tryOf(delegate) != null;

		float progress = 1 - Math.min(ticks / EntityExt.DISINTEGRATE_TICKS, 1);
		float alpha = 1 - Math.min((Math.max(0, progress - TRANSLUCENCY_START_PROGRESS) / (1 - TRANSLUCENCY_START_PROGRESS)) * 3, 1);
		this.packedColor = ColorABGR.pack(DARKEN, DARKEN, DARKEN, alpha);
		this.delta = Math.min(progress * (1 + TRANSLUCENCY_START_PROGRESS), 1);
	}

	@NotNull
	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		delegate.vertex(x, y, z);
		return this;
	}

	@NotNull
	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		int packedColor = ColorMixer.mix(this.packedColor, ColorABGR.pack(red, green, blue, alpha), this.delta);
		delegate.color(
				ColorABGR.unpackRed(packedColor),
				ColorABGR.unpackGreen(packedColor),
				ColorABGR.unpackBlue(packedColor),
				ColorABGR.unpackAlpha(packedColor)
		);
		return this;
	}

	@NotNull
	@Override
	public VertexConsumer uv(float u, float v) {
		delegate.uv(u, v);
		return this;
	}

	@NotNull
	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		delegate.overlayCoords(u, v);
		return this;
	}

	@NotNull
	@Override
	public VertexConsumer uv2(int u, int v) {
		delegate.uv2(u, v);
		return this;
	}

	@NotNull
	@Override
	public VertexConsumer normal(float x, float y, float z) {
		delegate.normal(x, y, z);
		return this;
	}

	@Override
	public void endVertex() {
		delegate.endVertex();
	}

	@Override
	public void defaultColor(int red, int green, int blue, int alpha) {
		delegate.defaultColor(red, green, blue, alpha);
	}

	@Override
	public void unsetDefaultColor() {
		delegate.unsetDefaultColor();
	}

	@Override
	public void push(MemoryStack stack, long ptr, int count, VertexFormatDescription format) {
		long stride = format.stride();
		long offsetColor = format.getElementOffset(CommonVertexAttribute.COLOR);
		for (int vertexIndex = 0; vertexIndex < count; vertexIndex++) {
			long attributePtr = (ptr + (stride * vertexIndex)) + offsetColor;
			ColorAttribute.set(
					attributePtr,
					ColorMixer.mix(this.packedColor, ColorAttribute.get(attributePtr), this.delta)
			);
		}
		VertexBufferWriter.of(delegate).push(stack, ptr, count, format);
	}

	@Override
	public boolean canUseIntrinsics() {
		return canUseIntrinsics;
	}
}
