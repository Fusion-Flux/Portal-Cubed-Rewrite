package io.github.fusionflux.portalcubed.framework.model;

import io.github.fusionflux.portalcubed.framework.model.blendmode.MultiBlendModeWrapper;
import io.github.fusionflux.portalcubed.framework.model.dynamictexture.DynamicTextureWrapper;
import io.github.fusionflux.portalcubed.framework.model.emissive.EmissiveData;
import io.github.fusionflux.portalcubed.framework.model.emissive.EmissiveWrapper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.Context;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.event.Event;

public enum PortalCubedModelLoadingPlugin implements PreparableModelLoadingPlugin<EmissiveData> {
	INSTANCE;

	@Override
	public void initialize(EmissiveData emissiveData, Context ctx) {
		Event<ModelModifier.AfterBake> modifyEvent = ctx.modifyModelAfterBake();
		modifyEvent.register(ModelModifier.WRAP_PHASE, new DynamicTextureWrapper());
		modifyEvent.register(ModelModifier.WRAP_PHASE, new MultiBlendModeWrapper());
		modifyEvent.register(ModelModifier.WRAP_PHASE, new EmissiveWrapper(emissiveData));
	}
}
