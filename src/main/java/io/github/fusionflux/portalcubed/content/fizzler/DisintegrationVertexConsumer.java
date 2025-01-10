package io.github.fusionflux.portalcubed.content.fizzler;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import io.github.fusionflux.portalcubed.framework.extension.EntityExt;
import io.github.fusionflux.portalcubed.framework.util.DelegatingVertexConsumer;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorMixer;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.minecraft.client.renderer.RenderType;

public class DisintegrationVertexConsumer extends DelegatingVertexConsumer {
	private static final float DARKEN = 0.15f;
	private static final float TRANSLUCENCY_START_PROGRESS = (EntityExt.DISINTEGRATE_TICKS - EntityExt.TRANSLUCENCY_START_TICKS) / (float) EntityExt.DISINTEGRATE_TICKS;

	// Compare names and not the objects because all entity render types create a new object
	private static final Set<String> DONT_DARKEN_RENDER_TYPES = ImmutableSet.of("eyes", "entity_translucent_emissive", "beacon_beam");

	private final int packedColor;
	private final float delta;

	public DisintegrationVertexConsumer(VertexConsumer delegate, RenderType renderType, float ticks) {
		this.delegate = delegate;

		float progress = 1 - Math.min(ticks / EntityExt.DISINTEGRATE_TICKS, 1);
		float alpha = 1 - Math.min((Math.max(0, progress - TRANSLUCENCY_START_PROGRESS) / (1 - TRANSLUCENCY_START_PROGRESS)) * 3, 1);
		this.packedColor = DONT_DARKEN_RENDER_TYPES.contains(renderType.name) ? ColorABGR.withAlpha(0xFFFFFF, alpha) : ColorABGR.pack(DARKEN, DARKEN, DARKEN, alpha);
		this.delta = Math.min(progress * (1 + TRANSLUCENCY_START_PROGRESS), 1);
	}

	@Override
	@NotNull
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		int packedColor = ColorMixer.mix(this.packedColor, ColorABGR.pack(red, green, blue, alpha), this.delta);
		this.delegate.setColor(
				ColorABGR.unpackRed(packedColor),
				ColorABGR.unpackGreen(packedColor),
				ColorABGR.unpackBlue(packedColor),
				ColorABGR.unpackAlpha(packedColor)
		);
		return this;
	}

	@Override
	public void push(MemoryStack stack, long ptr, int count, VertexFormat format) {
		long stride = format.getVertexSize();
		long offsetColor = format.getOffset(VertexFormatElement.COLOR);
		for (int vertexIndex = 0; vertexIndex < count; vertexIndex++) {
			long attributePtr = (ptr + (stride * vertexIndex)) + offsetColor;
			ColorAttribute.set(
					attributePtr,
					ColorMixer.mix(this.packedColor, ColorAttribute.get(attributePtr), this.delta)
			);
		}
		super.push(stack, ptr, count, format);
	}
}
