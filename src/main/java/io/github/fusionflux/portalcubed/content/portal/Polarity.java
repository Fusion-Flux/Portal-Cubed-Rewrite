package io.github.fusionflux.portalcubed.content.portal;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum Polarity implements StringRepresentable {
	PRIMARY(0xff2492fc),
	SECONDARY(0xffff8e1e);

	public static final Codec<Polarity> CODEC = StringRepresentable.fromEnum(Polarity::values);
	public static final StreamCodec<ByteBuf, Polarity> STREAM_CODEC = PortalCubedStreamCodecs.ofEnum(Polarity.class);

	public final String name;
	public final Component component;
	public final int defaultColor;

	Polarity(int defaultColor) {
		this.name = this.name().toLowerCase(Locale.ROOT);
		this.component = Component.translatable("misc.portalcubed.polarity." + this.name);
		this.defaultColor = defaultColor;
	}

	public Polarity opposite() {
		return switch (this) {
			case PRIMARY -> SECONDARY;
			case SECONDARY -> PRIMARY;
		};
	}

	@Override
	@NotNull
	public String getSerializedName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
