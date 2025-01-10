package io.github.fusionflux.portalcubed.content;

import java.util.Optional;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.lemon.LemonTrunkPlacer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class PortalCubedFeatures {
	public static final ResourceKey<PlacedFeature> ORE_MAGNESIUM = placed("ore_magnesium");

	public static final ResourceKey<ConfiguredFeature<?, ?>> LEMON_TREE = configured("lemon_tree");
	public static final TreeGrower LEMON_TREE_GROWER = new TreeGrower("lemon", Optional.empty(), Optional.of(LEMON_TREE), Optional.empty());

	public static ResourceKey<ConfiguredFeature<?, ?>> configured(String name) {
		return ResourceKey.create(Registries.CONFIGURED_FEATURE, PortalCubed.id(name));
	}

	public static ResourceKey<PlacedFeature> placed(String name) {
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
