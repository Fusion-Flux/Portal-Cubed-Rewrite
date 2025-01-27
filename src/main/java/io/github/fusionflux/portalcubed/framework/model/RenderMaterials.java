package io.github.fusionflux.portalcubed.framework.model;

import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonParseException;

import io.github.fusionflux.portalcubed.PortalCubed;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.Optionull;
import net.minecraft.Util;

public class RenderMaterials {
	@Nullable
	private static final MaterialFinder finder = Util.make(() -> {
		Renderer renderer;
		try {
			renderer = Renderer.get();
		} catch (UnsupportedOperationException e) {
			return null;
		}
		return renderer.materialFinder();
	});

	public static final boolean ARE_SUPPORTED = checkSupport();

	public static final RenderMaterial STANDARD = Optionull.map(finder, MaterialFinder::find);
	public static final RenderMaterial NO_AO = Optionull.map(finder, $ -> finder.ambientOcclusion(TriState.FALSE).find());

	private static final Object2ObjectOpenHashMap<String, BlendMode> BLEND_MODES = Util.make(new Object2ObjectOpenHashMap<>(), map -> {
		for (BlendMode mode : BlendMode.values()) {
			map.put(mode.name().toLowerCase(Locale.ROOT), mode);
		}
	});

	public static BlendMode parseBlendMode(String name) {
		BlendMode mode = BLEND_MODES.get(name);
		if (mode == null)
			throw new JsonParseException(String.format("Invalid blend mode \"%s\"; must be one of: %s", name, String.join(", ", BLEND_MODES.keySet())));
		return mode;
	}

	public static MaterialFinder finder() {
		if (!ARE_SUPPORTED)
			throw new IllegalStateException("Tried to access material finder with no renderer present!");
		return finder;
	}

	public static RenderMaterial makeEmissive(RenderMaterial material) {
		return finder()
				.copyFrom(material)
				.emissive(true)
				.disableDiffuse(true)
				.ambientOcclusion(TriState.FALSE)
				.find();
	}

	private static boolean checkSupport() {
		if (finder == null) {
			PortalCubed.LOGGER.error("No renderer present, rendering will be wrong. If you have Sodium, install Indium!");
			return false;
		}
		return true;
	}
}
