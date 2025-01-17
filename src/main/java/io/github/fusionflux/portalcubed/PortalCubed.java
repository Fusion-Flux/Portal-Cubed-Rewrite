package io.github.fusionflux.portalcubed;

import io.github.fusionflux.portalcubed.content.PortalCubedGameEvents;
import io.github.fusionflux.portalcubed.content.PortalCubedStats;

import io.github.fusionflux.portalcubed.content.fizzler.tool.FizzleinatorItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fusionflux.portalcubed.content.PortalCubedArgumentTypes;
import io.github.fusionflux.portalcubed.content.PortalCubedAttributes;
import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedCommands;
import io.github.fusionflux.portalcubed.content.PortalCubedCriteriaTriggers;
import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.PortalCubedFeatures;
import io.github.fusionflux.portalcubed.content.PortalCubedFluids;
import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedParticles;
import io.github.fusionflux.portalcubed.content.PortalCubedReloadListeners;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.PortalCubedTabs;
import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationSoundType;
import io.github.fusionflux.portalcubed.content.lemon.LemonadeItem;
import io.github.fusionflux.portalcubed.content.misc.MOTL;
import io.github.fusionflux.portalcubed.data.loot.PortalCubedLootFunctions;
import io.github.fusionflux.portalcubed.framework.block.HammerableBlock;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.extension.EntityExt;
import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import io.github.fusionflux.portalcubed.framework.signage.SignageManager;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.ResourceLocation;

public class PortalCubed implements ModInitializer {
	public static final String ID = "portalcubed";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static final Registrar REGISTRAR = new Registrar(ID);

	@Override
	public void onInitialize() {
		ModContainer container = FabricLoader.getInstance().getModContainer(ID).orElseThrow();
		String version = container.getMetadata().getVersion().toString();
		LOGGER.info("Portal Cubed ({}) initializing...", version);
		LOGGER.info(MOTL.get());

		PortalCubedGameRules.init();
		PortalCubedFluids.init();
		PortalCubedBlocks.init();
		PortalCubedBlockEntityTypes.init();
		PortalCubedDataComponents.init();
		PortalCubedAttributes.init();
		PortalCubedLootFunctions.init();
		PortalCubedItems.init();
		PortalCubedTabs.init();
		PortalCubedEntities.init();
		PortalCubedFeatures.init();
		PortalCubedSounds.init();
		PortalCubedParticles.init();
		PortalCubedCommands.init();
		PortalCubedPackets.init();
		PortalCubedArgumentTypes.init();
		PortalCubedCriteriaTriggers.init();
		PortalCubedStats.init();
		PortalCubedGameEvents.init();

		ConstructManager.registerEventListeners();
		SignageManager.registerEventListeners();
		EntityExt.registerEventListeners();
		HammerableBlock.registerEventListeners();
		LemonadeItem.registerEventListeners();
		FizzleinatorItem.registerEventListeners();
		DisintegrationSoundType.init();

		PortalCubedReloadListeners.registerData();

		LOGGER.info("Portal Cubed initialized!");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}
}
