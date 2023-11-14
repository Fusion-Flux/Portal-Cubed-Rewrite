package io.github.fusionflux.portalcubed.framework.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.Context;
import io.github.fusionflux.portalcubed.framework.model.emissive.EmissiveData;
import io.github.fusionflux.portalcubed.framework.model.emissive.EmissiveWrapper;
import io.github.fusionflux.portalcubed.framework.model.rendertype.MultiRenderTypeWrapper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;

public enum PortalCubedModelLoadingPlugin implements PreparableModelLoadingPlugin<EmissiveData> {
	INSTANCE;

	@Override
	public void onInitializeModelLoader(EmissiveData emissiveData, Context ctx) {
		ctx.modifyModelBeforeBake().register(ModelModifier.WRAP_PHASE, new MultiRenderTypeWrapper());
		ctx.modifyModelAfterBake().register(ModelModifier.WRAP_PHASE, new EmissiveWrapper(emissiveData));
	}
}
