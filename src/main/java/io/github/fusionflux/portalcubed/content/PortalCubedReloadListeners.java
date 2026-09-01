package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.content.portal.gun.crosshair.PortalGunCrosshairTypeManager;
import io.github.fusionflux.portalcubed.content.portal.gun.skin.PortalGunSkinManager;
import io.github.fusionflux.portalcubed.content.prop.renderer.PropModelCache;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.minecraft.server.packs.PackType;

public class PortalCubedReloadListeners {
	public static void registerData() {
		ResourceLoader loader = ResourceLoader.get(PackType.SERVER_DATA);
		loader.registerReloadListener(ConstructManager.ID, ConstructManager.INSTANCE);
	}

	public static void registerAssets() {
		ResourceLoader loader = ResourceLoader.get(PackType.CLIENT_RESOURCES);

		loader.registerReloadListener(PropModelCache.ID, PropModelCache.INSTANCE);
		loader.registerReloadListener(PortalGunCrosshairTypeManager.ID, PortalGunCrosshairTypeManager.INSTANCE);
		loader.registerReloadListener(PortalGunSkinManager.ID, PortalGunSkinManager.INSTANCE);
//		loader.registerReloadListener(PortalTextureManager.ID, PortalTextureManager.INSTANCE);
//		loader.registerReloadListener(PortalStencilRenderer.ID, PortalStencilRenderer.INSTANCE);

		loader.addListenerOrdering(ResourceReloaderKeys.Client.MODELS, PropModelCache.ID);
//		loader.addListenerOrdering(PortalTextureManager.ID, PortalStencilRenderer.ID);
	}
}
