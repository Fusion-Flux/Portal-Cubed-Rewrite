package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.content.prop.renderer.PropModelCache;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.signage.SignageManager;
import io.github.fusionflux.portalcubed.framework.util.ShaderPatcher;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class PortalCubedReloadListeners {
	public static void registerData() {
		ResourceManagerHelper helper = ResourceManagerHelper.get(PackType.SERVER_DATA);
		helper.registerReloadListener(SignageManager.INSTANCE);
		helper.registerReloadListener(ConstructManager.INSTANCE);
	}

	public static void registerAssets() {
		ResourceManagerHelper helper = ResourceManagerHelper.get(PackType.CLIENT_RESOURCES);
		helper.registerReloadListener(ShaderPatcher.ReloadListener.INSTANCE);
		helper.registerReloadListener(PropModelCache.INSTANCE);
	}
}
