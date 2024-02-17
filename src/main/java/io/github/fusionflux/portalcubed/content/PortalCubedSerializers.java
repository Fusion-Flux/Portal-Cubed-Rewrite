package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.sounds.SoundEvent;

import org.quiltmc.qsl.entity.extensions.api.networking.QuiltTrackedDataHandlerRegistry;

public class PortalCubedSerializers {
	public static final EntityDataSerializer<PortalData> PORTAL_DATA = register(
			"portal_data", EntityDataSerializer.simple(PortalData::toNetwork, PortalData::fromNetwork)
	);

	public static final EntityDataSerializer<SoundEvent> SOUND_EVENT = register(
			"sound_event", EntityDataSerializer.simple((buf, soundEvent) -> soundEvent.writeToNetwork(buf), SoundEvent::readFromNetwork)
	);

	private static <T> EntityDataSerializer<T> register(String name, EntityDataSerializer<T> serializer) {
		return QuiltTrackedDataHandlerRegistry.register(PortalCubed.id(name), serializer);
	}

	public static void init() {
	}
}
