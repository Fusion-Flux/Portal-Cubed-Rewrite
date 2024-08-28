package io.github.fusionflux.portalcubed.framework.model;

import io.github.fusionflux.portalcubed.framework.model.dynamictexture.DynamicTextureWrapper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.Context;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

import com.google.common.collect.ImmutableSet;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.model.emissive.EmissiveData;
import io.github.fusionflux.portalcubed.framework.model.emissive.EmissiveWrapper;
import io.github.fusionflux.portalcubed.framework.model.blendmode.MultiBlendModeWrapper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;

public enum PortalCubedModelLoadingPlugin implements PreparableModelLoadingPlugin<EmissiveData> {
	INSTANCE;

	private static final Collection<ResourceLocation> FIRE_MODELS = Util.make(() -> {
		ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();
		var types = new String[]{
			"floor0",
			"floor1",
			"side_alt0",
			"side_alt1",
			"side0",
			"side1",
			"up_alt0",
			"up_alt1",
			"up0",
			"up1",
		};
		for (var type : types) {
			builder.add(new ResourceLocation("block/fire_" + type));
		}
		return builder.build();
	});

	// The selector that is currently being used within multipart model baking, null when not. Never use this outside of modifyModelAfterBake
	public static Selector currentSelectorBaking;

	@Override
	public void onInitializeModelLoader(EmissiveData emissiveData, Context ctx) {
		Event<ModelModifier.AfterBake> modifyEvent = ctx.modifyModelAfterBake();
		modifyEvent.register(ModelModifier.WRAP_PHASE, new DynamicTextureWrapper());
		modifyEvent.register(ModelModifier.WRAP_PHASE, new MultiBlendModeWrapper());
		modifyEvent.register(ModelModifier.WRAP_PHASE, new EmissiveWrapper(emissiveData));

		for (ResourceLocation model : FIRE_MODELS) {
			ctx.addModels(PortalCubed.id(model.getPath().replace("fire", "magnesium_fire")));
		}
		modifyEvent.register(ModelModifier.DEFAULT_PHASE, (model, context) -> {
			ResourceLocation modelId = context.id();
			if (currentSelectorBaking != null && FIRE_MODELS.contains(modelId)) {
				ResourceLocation magnesiumVariantId = PortalCubed.id(modelId.getPath().replace("fire", "magnesium_fire"));
				BakedModel magnesiumVariant = context.baker().bake(magnesiumVariantId, context.settings());
				if (magnesiumVariant != null)
					return new MagnesiumFireModel(model, magnesiumVariant, currentSelectorBaking);
			}
			return model;
		});
	}
}
