package io.github.fusionflux.portalcubed.content.panel;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public enum PanelMaterial {
	PORTAL_1_WHITE,
	DIRTY_PORTAL_1_WHITE,
	PORTAL_1_METAL,
	DIRTY_PORTAL_1_METAL,
	WHITE,
	AGED_WHITE,
	PADDED_GRAY,
	AGED_PADDED_GRAY,
	SMOOTH_GRAY,
	AGED_SMOOTH_GRAY,
	OLD_AP_WHITE,
	OLD_AP_GREEN,
	OLD_AP_BLUE;

	// maybe we'll do something with this eventually, kept mostly as a record for now
	// DFU? It has major issues.
	public static final Map<String, String> NAME_MIGRATIONS = Map.of(
			"portal_1_smooth_gray", PORTAL_1_METAL.name,
			"dirty_portal_1_smooth_gray", DIRTY_PORTAL_1_METAL.name
	);

	public final String name;
	public final boolean hasCheckered;
	public final boolean hasJoiner;
	public final List<PanelPart> parts;

	private final QuiltBlockSettings settings;

	PanelMaterial() {
		this(settings -> {});
	}

	PanelMaterial(Consumer<QuiltBlockSettings> settingsModifier) {
		this.name = this.name().toLowerCase(Locale.ROOT);
		this.hasCheckered = this.name.contains("white");
		this.hasJoiner = this.name.contains("portal_1_metal");
		this.parts = Arrays.stream(PanelPart.values())
				.filter(part -> part != PanelPart.CHECKERED || this.hasCheckered)
				.filter(part -> part != PanelPart.JOINER || this.hasJoiner)
				.toList();

		this.settings = QuiltBlockSettings.create();
		settingsModifier.accept(this.settings);
	}

	public QuiltBlockSettings getSettings() {
		return QuiltBlockSettings.copyOf(this.settings);
	}
}
