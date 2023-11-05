package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalShape;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import net.minecraft.network.syncher.EntityDataSerializer;

import org.quiltmc.qsl.entity.networking.api.tracked_data.QuiltTrackedDataHandlerRegistry;

public class PortalCubedSerializers {
	public static final EntityDataSerializer<PortalData> PORTAL_DATA = register(
			"portal_data", EntityDataSerializer.simple(PortalData::toNetwork, PortalData::fromNetwork)
	);

	private static <T> EntityDataSerializer<T> register(String name, EntityDataSerializer<T> serializer) {
		return QuiltTrackedDataHandlerRegistry.register(PortalCubed.id(name), serializer);
	}

	public static void init() {
	}
}
