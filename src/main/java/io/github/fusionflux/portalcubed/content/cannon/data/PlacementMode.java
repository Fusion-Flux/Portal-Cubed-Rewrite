package io.github.fusionflux.portalcubed.content.cannon.data;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum PlacementMode implements StringRepresentable {
	SINGLE,
	AUTO,
	WHOLE;

	public static final Codec<PlacementMode> CODEC = StringRepresentable.fromEnum(PlacementMode::values);

	private final String name = this.name().toLowerCase(Locale.ROOT);

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
