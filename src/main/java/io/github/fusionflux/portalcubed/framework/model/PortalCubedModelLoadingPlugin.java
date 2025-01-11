package io.github.fusionflux.portalcubed.framework.model;

import io.github.fusionflux.portalcubed.framework.model.blendmode.MultiBlendModeWrapper;
import io.github.fusionflux.portalcubed.framework.model.dynamictexture.DynamicTextureWrapper;
import io.github.fusionflux.portalcubed.framework.model.emissive.EmissiveData;
import io.github.fusionflux.portalcubed.framework.model.emissive.EmissiveWrapper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.Context;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.renderer.block.model.multipart.Selector;

public enum PortalCubedModelLoadingPlugin implements PreparableModelLoadingPlugin<EmissiveData> {
	INSTANCE;

//	private static final Collection<ResourceLocation> FIRE_MODELS = Util.make(() -> {
//		ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();
//		for (String type : new String[]{
//				"floor0",
//				"floor1",
//				"side_alt0",
//				"side_alt1",
//				"side0",
//				"side1",
//				"up_alt0",
//				"up_alt1",
//				"up0",
//				"up1",
//		}) {
//			builder.add(ResourceLocation.withDefaultNamespace("block/fire_" + type));
//		}
//		return builder.build();
//	});

	// The selector that is currently being used within multipart model baking, null when not. Never use this outside of modifyModelAfterBake
	public static Selector currentSelectorBaking;

	@Override
	public void initialize(EmissiveData emissiveData, Context ctx) {
		Event<ModelModifier.AfterBake> modifyEvent = ctx.modifyModelAfterBake();
		modifyEvent.register(ModelModifier.WRAP_PHASE, new DynamicTextureWrapper());
		modifyEvent.register(ModelModifier.WRAP_PHASE, new MultiBlendModeWrapper());
		modifyEvent.register(ModelModifier.WRAP_PHASE, new EmissiveWrapper(emissiveData));

//		for (ResourceLocation model : FIRE_MODELS) {
//			ctx.addModels(PortalCubed.id(model.getPath().replace("fire", "magnesium_fire")));
//		}
//		modifyEvent.register(ModelModifier.DEFAULT_PHASE, (model, context) -> {
//			ResourceLocation modelId = context.id();
//			if (currentSelectorBaking != null && FIRE_MODELS.contains(modelId)) {
//				ResourceLocation magnesiumVariantId = PortalCubed.id(modelId.getPath().replace("fire", "magnesium_fire"));
//				BakedModel magnesiumVariant = context.baker().bake(magnesiumVariantId, context.settings());
//				if (magnesiumVariant != null)
//					return new MagnesiumFireModel(model, magnesiumVariant, currentSelectorBaking);
//			}
//			return model;
//		});
	}
}
