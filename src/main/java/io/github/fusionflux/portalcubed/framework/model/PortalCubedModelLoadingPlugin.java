package io.github.fusionflux.portalcubed.framework.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.Context;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.model.emissive.EmissiveData;
import io.github.fusionflux.portalcubed.framework.model.emissive.EmissiveWrapper;
import io.github.fusionflux.portalcubed.framework.model.rendertype.MultiRenderTypeWrapper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;

public enum PortalCubedModelLoadingPlugin implements PreparableModelLoadingPlugin<EmissiveData> {
	INSTANCE;

	public static Selector currentSelectorBaking;

	@Override
	public void onInitializeModelLoader(EmissiveData emissiveData, Context ctx) {
		ctx.modifyModelBeforeBake().register(ModelModifier.WRAP_PHASE, new MultiRenderTypeWrapper());
		ctx.modifyModelAfterBake().register(ModelModifier.WRAP_PHASE, new EmissiveWrapper(emissiveData));

		ctx.addModels(
			PortalCubed.id("block/magnesium_fire_floor0"),
			PortalCubed.id("block/magnesium_fire_floor1"),
			PortalCubed.id("block/magnesium_fire_side_alt0"),
			PortalCubed.id("block/magnesium_fire_side_alt1"),
			PortalCubed.id("block/magnesium_fire_side0"),
			PortalCubed.id("block/magnesium_fire_side1"),
			PortalCubed.id("block/magnesium_fire_up_alt0"),
			PortalCubed.id("block/magnesium_fire_up_alt1"),
			PortalCubed.id("block/magnesium_fire_up0"),
			PortalCubed.id("block/magnesium_fire_up1")
		);
		ctx.modifyModelAfterBake().register(ModelModifier.OVERRIDE_PHASE, (model, context) -> {
			var id = context.id();
			var namespace = id.getNamespace();
			var path = id.getPath();
			if (currentSelectorBaking != null && (namespace.equals("minecraft") && path.startsWith("block/fire") && !path.contains("coral"))) {
				var magnesiumVariantId = PortalCubed.id(path.replace("fire", "magnesium_fire"));
				if (magnesiumVariantId != null) {
					var magnesiumVariant = context.baker().bake(magnesiumVariantId, context.settings());
					if (magnesiumVariant != null)
						return new MagnesiumFireModel(model, magnesiumVariant, currentSelectorBaking);
				}
			}
			return model;
		});
	}
}
