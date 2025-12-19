package io.github.fusionflux.portalcubed.content.portal.command.argument.portal;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.graphics.color.PortalColor;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.PortalValidator;
import net.minecraft.util.Unit;

public final class PortalAttribute<T> {
	public static final Map<String, PortalAttribute<?>> REGISTRY = new HashMap<>();

	public static final PortalAttribute<PortalColor> COLOR = new PortalAttribute<>("color", PortalColor.CODEC, PortalData::withColor);
	public static final PortalAttribute<PortalValidator> VALIDATOR = new PortalAttribute<>("validator", PortalValidator.CODEC, PortalData::withValidator);
	public static final PortalAttribute<Unit> NO_RENDERING = new PortalAttribute<>("no_rendering", Unit.CODEC, (data, $) -> data.withRender(false));
	public static final PortalAttribute<Unit> NO_TRACER = new PortalAttribute<>("no_tracer", Unit.CODEC, (data, $) -> data.withTracer(false));

	public final String name;
	public final Codec<T> codec;
	private final Applicator<T> applicator;

	private PortalAttribute(String name, Codec<T> codec, Applicator<T> applicator) {
		this.name = name;
		this.codec = codec;
		this.applicator = applicator;

		if (REGISTRY.containsKey(name)) {
			throw new IllegalArgumentException("Duplicate attribute: " + name);
		}

		REGISTRY.put(name, this);
	}

	public PortalData modify(PortalData data, T value) {
		return this.applicator.modify(data, value);
	}

	private interface Applicator<T> {
		PortalData modify(PortalData data, T value);
	}
}
