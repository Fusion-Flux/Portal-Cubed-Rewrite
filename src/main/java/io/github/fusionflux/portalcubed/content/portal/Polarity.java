package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum Polarity implements StringRepresentable {
	PRIMARY(0xff2492fc),
	SECONDARY(0xffff8e1e);

	public static final Codec<Polarity> CODEC = StringRepresentable.fromEnum(Polarity::values);

	public final String name;
	public final int defaultColor;

	Polarity(int defaultColor) {
		this.name = name().toLowerCase(Locale.ROOT);
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
