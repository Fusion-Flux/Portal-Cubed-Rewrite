package io.github.fusionflux.portalcubed.content;

import org.quiltmc.qsl.worldgen.biome.api.BiomeModifications;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectors;
import org.quiltmc.qsl.worldgen.biome.api.ModificationPhase;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class PortalCubedFeatures {
	public static final ResourceKey<PlacedFeature> ORE_MAGNESIUM = create("ore_magnesium");

	public static ResourceKey<PlacedFeature> create(String name) {
		return ResourceKey.create(Registries.PLACED_FEATURE, PortalCubed.id(name));
	}

	public static void init() {
		BiomeModifications.create(PortalCubed.id("features"))
			.add(ModificationPhase.ADDITIONS, BiomeSelectors.foundInOverworld(), (selectionCtx, modificationCtx) -> {
				modificationCtx.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, ORE_MAGNESIUM);
			});
	}
}
