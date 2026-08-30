package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.lemon.LemonTrunkPlacer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class PortalCubedFeatures {
	public static final ResourceKey<PlacedFeature> ORE_MAGNESIUM = placedKey("ore_magnesium");

	public static final ResourceKey<Feature> LEMON_TREE = key("lemon_tree");
	public static final TreeGrower LEMON_TREE_GROWER = new TreeGrower("lemon", WeightedList.of(LEMON_TREE), WeightedList.of(), WeightedList.of(), LEMON_TREE);

	public static ResourceKey<Feature> key(String name) {
		return ResourceKey.create(Registries.FEATURE, PortalCubed.id(name));
	}

	public static ResourceKey<PlacedFeature> placedKey(String name) {
		return ResourceKey.create(Registries.PLACED_FEATURE, PortalCubed.id(name));
	}

	public static void init() {
		LemonTrunkPlacer.init();
		BiomeModifications.create(PortalCubed.id("features"))
			.add(
					ModificationPhase.ADDITIONS,
					BiomeSelectors.foundInOverworld(),
					(selectionCtx, modificationCtx) -> modificationCtx.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, ORE_MAGNESIUM)
			);
	}
}
