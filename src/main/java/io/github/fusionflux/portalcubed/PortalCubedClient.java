package io.github.fusionflux.portalcubed;

import io.github.fusionflux.portalcubed.content.PortalCubedColorProviders;
import io.github.fusionflux.portalcubed.content.PortalCubedScreens;
import io.github.fusionflux.portalcubed.content.portal.PortalRenderer;
import io.github.fusionflux.portalcubed.framework.model.PortalCubedModelLoadingPlugin;
import io.github.fusionflux.portalcubed.framework.model.emissive.EmissiveLoader;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class PortalCubedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		PortalRenderer.init();
		PortalCubedColorProviders.init();
		PortalCubedScreens.init();

		PreparableModelLoadingPlugin.register(EmissiveLoader.INSTANCE, PortalCubedModelLoadingPlugin.INSTANCE);
	}
}
