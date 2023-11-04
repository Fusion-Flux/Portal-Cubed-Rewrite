package io.github.fusionflux.portalcubed.content.portal;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public enum PortalShape {
	SQUARE, ROUND;

	public final ResourceLocation texture;
	public final ResourceLocation tracerTexture;

	PortalShape() {
		String name = name().toLowerCase(Locale.ROOT);
		this.texture = PortalCubed.id("textures/entity/portal/" + name + ".png");
		this.tracerTexture = PortalCubed.id("textures/entity/portal/tracer/" + name + ".png");
	}
}
