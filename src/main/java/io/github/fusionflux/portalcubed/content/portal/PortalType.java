package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum PortalType implements StringRepresentable {
	PRIMARY(0xff2492fc),
	SECONDARY(0xffff8e1e);

	public static final Codec<PortalType> CODEC = StringRepresentable.fromEnum(PortalType::values);

	public final String name;
	public final int defaultColor;

	PortalType(int defaultColor) {
		this.name = name().toLowerCase(Locale.ROOT);
		this.defaultColor = defaultColor;
	}

	public PortalType opposite() {
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
