package io.github.fusionflux.portalcubed.content.panel;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static io.github.fusionflux.portalcubed.content.panel.PanelMaterial.Flags.*;

public enum PanelMaterial {
	PORTAL_1_WHITE(CHECKERED, NO_2x2),
	DIRTY_PORTAL_1_WHITE(CHECKERED, NO_2x2),
	PORTAL_1_METAL(JOINER),
	DIRTY_PORTAL_1_METAL(JOINER),
	WHITE(CHECKERED),
	AGED_WHITE(CHECKERED),
	PADDED_GRAY,
	AGED_PADDED_GRAY,
	SMOOTH_GRAY,
	AGED_SMOOTH_GRAY,
	OLD_AP_WHITE(CHECKERED, NO_1x2, NO_HALF),
	OLD_AP_GREEN(NO_HALF),
	OLD_AP_BLUE(NO_HALF);

	// maybe we'll do something with this eventually, kept mostly as a record for now
	// DFU? It has major issues.
	public static final Map<String, String> NAME_MIGRATIONS = Map.of(
			"portal_1_smooth_gray", PORTAL_1_METAL.name,
			"dirty_portal_1_smooth_gray", DIRTY_PORTAL_1_METAL.name
	);

	public final String name;
	public final List<PanelPart> parts;

	private final QuiltBlockSettings settings;

	PanelMaterial(Flags... flags) {
		this(settings -> {}, flags);
	}

	PanelMaterial(Consumer<QuiltBlockSettings> settingsModifier, Flags... flags) {
		this.name = this.name().toLowerCase(Locale.ROOT);

		Set<Flags> set = Set.of(flags);
		this.parts = Arrays.stream(PanelPart.values())
				.filter(part -> switch (part) {
					case CHECKERED -> set.contains(CHECKERED);
					case JOINER -> set.contains(JOINER);
					case MULTI_2x2_BOTTOM_LEFT, MULTI_2x2_BOTTOM_RIGHT,
							MULTI_2x2_TOP_LEFT, MULTI_2x2_TOP_RIGHT -> !set.contains(NO_2x2);
					case MULTI_1x2_BOTTOM, MULTI_1x2_TOP -> !set.contains(NO_1x2);
					case HALF -> !set.contains(NO_HALF);
					default -> true;
				})
				.toList();

		this.settings = QuiltBlockSettings.create();
		settingsModifier.accept(this.settings);
	}

	public QuiltBlockSettings getSettings() {
		return QuiltBlockSettings.copyOf(this.settings);
	}

	enum Flags {
		CHECKERED,
		JOINER,
		NO_2x2,
		NO_1x2,
		NO_HALF
	}
}
