package io.github.fusionflux.portalcubed;

import io.github.fusionflux.portalcubed.content.PortalCubedColorProviders;
import io.github.fusionflux.portalcubed.content.PortalCubedParticlesClient;
import io.github.fusionflux.portalcubed.content.portal.PortalRenderer;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class PortalCubedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		PortalRenderer.init();
		PortalCubedColorProviders.init();
		PortalCubedParticlesClient.init();
	}
}
