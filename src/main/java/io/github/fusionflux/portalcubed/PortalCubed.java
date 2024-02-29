package io.github.fusionflux.portalcubed;

import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedSerializers;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.PortalCubedTabs;
import io.github.fusionflux.portalcubed.content.misc.MOTL;
import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.minecraft.resources.ResourceLocation;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalCubed implements ModInitializer {
	public static final String ID = "portalcubed";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static final Registrar REGISTRAR = new Registrar(ID);

	@Override
	public void onInitialize(ModContainer mod) {
		ModMetadata metadata = mod.metadata();
		LOGGER.info("Portal Cubed (" + metadata.version() + ") initializing...");
		LOGGER.info(MOTL.get());

		PortalCubedGameRules.init();

		PortalCubedBlocks.init();
		PortalCubedItems.init();
		PortalCubedTabs.init();
		PortalCubedEntities.init();
		PortalCubedSerializers.init();

		PortalCubedSounds.init();

		LOGGER.info("Portal Cubed (" + metadata.version() + ") initialized!");
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
