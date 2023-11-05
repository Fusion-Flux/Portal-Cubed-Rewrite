package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum PortalShape implements StringRepresentable {
	SQUARE, ROUND;

	public static final Codec<PortalShape> CODEC = StringRepresentable.fromEnum(PortalShape::values);

	public final String name;
	public final ResourceLocation texture;
	public final ResourceLocation tracerTexture;

	PortalShape() {
		this.name = name().toLowerCase(Locale.ROOT);
		this.texture = PortalCubed.id("textures/entity/portal/" + name + ".png");
		this.tracerTexture = PortalCubed.id("textures/entity/portal/tracer/" + name + ".png");
	}

	@Override
	@NotNull
	public String getSerializedName() {
		return this.name;
	}
}
