package io.github.fusionflux.portalcubed;

import java.util.List;

import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedMenus;
import io.github.fusionflux.portalcubed.content.PortalCubedSerializers;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.PortalCubedTabs;
import io.github.fusionflux.portalcubed.framework.construct.ConstructLoader;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.RandomSource;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
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
		Util.getRandomSafe(List.copyOf(metadata.contributors()), RandomSource.create()).ifPresent(contributor -> {
			LOGGER.info("I loved the part when " + contributor.name() + " said \"It's Portaln' time\" and portal'd all over the place");
		});

		PortalCubedEntityTags.init();

		PortalCubedBlocks.init();
		PortalCubedItems.init();
		PortalCubedTabs.init();
		PortalCubedEntities.init();
		PortalCubedSerializers.init();
		PortalCubedMenus.init();

		PortalCubedSounds.init();

		ResourceLoader.get(PackType.SERVER_DATA).registerReloader(ConstructLoader.INSTANCE);
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
