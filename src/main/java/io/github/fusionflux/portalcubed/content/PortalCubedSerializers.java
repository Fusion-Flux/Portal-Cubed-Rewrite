package io.github.fusionflux.portalcubed.content;

import org.quiltmc.qsl.entity.extensions.api.networking.QuiltTrackedDataHandlerRegistry;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.sounds.SoundEvent;

public class PortalCubedSerializers {
	public static final EntityDataSerializer<PortalSettings> PORTAL_SETTINGS = register(
			"portal_settings", EntityDataSerializer.simple(PortalSettings::toNetwork, PortalSettings::fromNetwork)
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
