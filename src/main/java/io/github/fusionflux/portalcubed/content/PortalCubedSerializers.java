package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import net.minecraft.network.syncher.EntityDataSerializer;

import org.quiltmc.qsl.entity.networking.api.tracked_data.QuiltTrackedDataHandlerRegistry;

public class PortalCubedSerializers {
	public static final EntityDataSerializer<PortalSettings> PORTAL_SETTINGS = register(
			"portal_settings", EntityDataSerializer.simple(PortalSettings::toNetwork, PortalSettings::fromNetwork)
	);

	private static <T> EntityDataSerializer<T> register(String name, EntityDataSerializer<T> serializer) {
		return QuiltTrackedDataHandlerRegistry.register(PortalCubed.id(name), serializer);
	}

	public static void init() {
	}
}
