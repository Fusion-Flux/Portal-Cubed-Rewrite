package io.github.fusionflux.portalcubed.framework.model;

import io.github.fusionflux.portalcubed.framework.model.dynamictexture.DynamicTextureWrapper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;

public enum PortalCubedModelLoadingPlugin implements ModelLoadingPlugin {
	INSTANCE;

	@Override
	public void initialize(Context pluginContext) {
		pluginContext.modifyBlockModelAfterBake()
				.register(ModelModifier.WRAP_PHASE, new DynamicTextureWrapper());
	}
}
