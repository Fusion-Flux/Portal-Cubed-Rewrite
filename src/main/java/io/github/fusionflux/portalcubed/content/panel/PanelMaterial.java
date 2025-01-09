package io.github.fusionflux.portalcubed.content.panel;

import static io.github.fusionflux.portalcubed.content.panel.PanelMaterial.Flags.CHECKERED;
import static io.github.fusionflux.portalcubed.content.panel.PanelMaterial.Flags.JOINER;
import static io.github.fusionflux.portalcubed.content.panel.PanelMaterial.Flags.NO_1x2;
import static io.github.fusionflux.portalcubed.content.panel.PanelMaterial.Flags.NO_2x2;
import static io.github.fusionflux.portalcubed.content.panel.PanelMaterial.Flags.NO_HALF;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public enum PanelMaterial {
	PORTAL_1_WHITE(PanelMaterial::p1WhiteSettings, CHECKERED, NO_2x2),
	DIRTY_PORTAL_1_WHITE(PanelMaterial::p1DirtyWhiteSettings, CHECKERED, NO_2x2),
	PORTAL_1_METAL(PanelMaterial::p1MetalSettings, JOINER),
	DIRTY_PORTAL_1_METAL(PanelMaterial::p1DirtyMetalSettings, JOINER),
	WHITE(PanelMaterial::whiteSettings, CHECKERED),
	AGED_WHITE(PanelMaterial::agedWhiteSettings, CHECKERED),
	PADDED_GRAY(PanelMaterial::paddedGraySettings),
	AGED_PADDED_GRAY(PanelMaterial::paddedGraySettings),
	SMOOTH_GRAY(PanelMaterial::smoothGraySettings),
	AGED_SMOOTH_GRAY(PanelMaterial::smoothGraySettings),
	OLD_AP_WHITE(PanelMaterial::oldApWhiteSettings, CHECKERED, NO_1x2, NO_HALF),
	OLD_AP_GREEN(PanelMaterial::oldApGreenSettings, NO_HALF),
	OLD_AP_BLUE(PanelMaterial::oldApBlueSettings, NO_HALF);

	// maybe we'll do something with this eventually, kept mostly as a record for now
	// DFU? It has major issues.
	public static final Map<String, String> NAME_MIGRATIONS = Map.of(
			"portal_1_smooth_gray", PORTAL_1_METAL.name,
			"dirty_portal_1_smooth_gray", DIRTY_PORTAL_1_METAL.name
	);

	public final String name;
	public final List<PanelPart> parts;

	private final QuiltBlockSettings settings;

	PanelMaterial(Supplier<QuiltBlockSettings> settingsCreator, Flags... flags) {
		this.name = this.name().toLowerCase(Locale.ROOT);

		Set<Flags> set = Set.of(flags);
		this.parts = Arrays.stream(PanelPart.values())
				.filter(part -> switch (part) {
					case CHECKERED, CHECKERED_SLAB, CHECKERED_STAIRS, CHECKERED_FACADE -> set.contains(CHECKERED);
					case JOINER -> set.contains(JOINER);
					case MULTI_2x2_BOTTOM_LEFT, MULTI_2x2_BOTTOM_RIGHT,
							MULTI_2x2_TOP_LEFT, MULTI_2x2_TOP_RIGHT -> !set.contains(NO_2x2);
					case MULTI_1x2 -> !set.contains(NO_1x2);
					case HALF, HALF_SLAB, HALF_STAIRS, HALF_FACADE -> !set.contains(NO_HALF);
					default -> true;
				})
				.toList();

		this.settings = settingsCreator.get();
	}

	public QuiltBlockSettings getSettings() {
		return QuiltBlockSettings.copyOf(this.settings);
	}

	public static QuiltBlockSettings p1WhiteSettings() {
		return QuiltBlockSettings.copyOf(Blocks.WHITE_CONCRETE).mapColor(MapColor.COLOR_LIGHT_GRAY);
	}

	public static QuiltBlockSettings p1DirtyWhiteSettings() {
		return p1WhiteSettings().mapColor(MapColor.TERRACOTTA_WHITE);
	}

	public static QuiltBlockSettings p1MetalSettings() {
		return QuiltBlockSettings.copyOf(Blocks.COPPER_BLOCK).mapColor(MapColor.TERRACOTTA_BROWN);
	}

	public static QuiltBlockSettings p1DirtyMetalSettings() {
		return p1MetalSettings().mapColor(MapColor.TERRACOTTA_GREEN);
	}

	public static QuiltBlockSettings whiteSettings() {
		return QuiltBlockSettings.copyOf(Blocks.STONE).mapColor(MapColor.WOOL).sounds(SoundType.CHERRY_WOOD);
	}

	public static QuiltBlockSettings agedWhiteSettings() {
		return whiteSettings().mapColor(MapColor.GRASS);
	}

	public static QuiltBlockSettings paddedGraySettings() {
		return QuiltBlockSettings.copyOf(Blocks.STONE).mapColor(MapColor.DEEPSLATE);
	}

	public static QuiltBlockSettings smoothGraySettings() {
		return QuiltBlockSettings.copyOf(Blocks.STONE).mapColor(MapColor.TERRACOTTA_CYAN);
	}

	public static QuiltBlockSettings oldApWhiteSettings() {
		return QuiltBlockSettings.copyOf(Blocks.OAK_PLANKS).mapColor(MapColor.SAND);
	}

	public static QuiltBlockSettings oldApGreenSettings() {
		return QuiltBlockSettings.copyOf(Blocks.COPPER_BLOCK).mapColor(MapColor.TERRACOTTA_LIGHT_GREEN).sounds(SoundType.NETHERITE_BLOCK);
	}

	public static QuiltBlockSettings oldApBlueSettings() {
		return oldApGreenSettings().mapColor(MapColor.COLOR_CYAN);
	}

	enum Flags {
		CHECKERED,
		JOINER,
		NO_2x2,
		NO_1x2,
		NO_HALF
	}
}
