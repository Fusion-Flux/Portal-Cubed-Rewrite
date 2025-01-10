package io.github.fusionflux.portalcubed.content.portal;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public enum PortalShape implements StringRepresentable {
	SQUARE, ROUND;

	public static final Codec<PortalShape> CODEC = StringRepresentable.fromEnum(PortalShape::values);
	public static final StreamCodec<ByteBuf, PortalShape> STREAM_CODEC = PortalCubedStreamCodecs.ofEnum(PortalShape.class);

	public final String name;
	public final ResourceLocation texture;
	public final ResourceLocation tracerTexture;
	public final ResourceLocation stencilTexture;

	PortalShape() {
		this.name = name().toLowerCase(Locale.ROOT);
		this.texture = PortalCubed.id("textures/entity/portal/" + name + ".png");
		this.tracerTexture = PortalCubed.id("textures/entity/portal/tracer/" + name + ".png");
		this.stencilTexture = PortalCubed.id("textures/entity/portal/stencil/" + name + ".png");
	}

	@Override
	@NotNull
	public String getSerializedName() {
		return this.name;
	}
}
