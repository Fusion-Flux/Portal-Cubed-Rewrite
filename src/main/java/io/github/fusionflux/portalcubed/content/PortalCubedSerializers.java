package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.PortalShape;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import net.minecraft.network.syncher.EntityDataSerializer;

import org.quiltmc.qsl.entity.networking.api.tracked_data.QuiltTrackedDataHandlerRegistry;

public class PortalCubedSerializers {
	public static final EntityDataSerializer<PortalType> PORTAL_TYPE = register("portal_type", EntityDataSerializer.simpleEnum(PortalType.class));
	public static final EntityDataSerializer<PortalShape> PORTAL_SHAPE = register("portal_shape", EntityDataSerializer.simpleEnum(PortalShape.class));

	private static <T> EntityDataSerializer<T> register(String name, EntityDataSerializer<T> serializer) {
		return QuiltTrackedDataHandlerRegistry.register(PortalCubed.id(name), serializer);
	}

	public static void init() {
	}
}
