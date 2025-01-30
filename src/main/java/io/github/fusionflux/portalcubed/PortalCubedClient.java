package io.github.fusionflux.portalcubed;

import com.terraformersmc.terraform.boat.api.client.TerraformBoatClientHelper;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.PortalCubedFluids;
import io.github.fusionflux.portalcubed.content.PortalCubedKeyMappings;
import io.github.fusionflux.portalcubed.content.PortalCubedReloadListeners;
import io.github.fusionflux.portalcubed.content.boots.SourcePhysics;
import io.github.fusionflux.portalcubed.content.cannon.ConstructPreviewRenderer;
import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonAnimator;
import io.github.fusionflux.portalcubed.content.lemon.Armed;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunTintSource;
import io.github.fusionflux.portalcubed.content.portal.gun_pedestal.PortalGunPedestalModel;
import io.github.fusionflux.portalcubed.content.portal.renderer.PortalRenderer;
import io.github.fusionflux.portalcubed.content.prop.renderer.PropVariantProperty;
import io.github.fusionflux.portalcubed.framework.entity.EntityDebugRendering;
import io.github.fusionflux.portalcubed.framework.model.PortalCubedModelLoadingPlugin;
import io.github.fusionflux.portalcubed.framework.model.emissive.EmissiveLoader;
import io.github.fusionflux.portalcubed.framework.render.debug.DebugRendering;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;

public class PortalCubedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PortalRenderer.init();
		EntityDebugRendering.init();
		DebugRendering.init();
		ConstructPreviewRenderer.init();
		PortalCubedKeyMappings.init();

		FluidRenderHandlerRegistry.INSTANCE.register(
				PortalCubedFluids.GOO,
				PortalCubedFluids.FLOWING_GOO,
				new SimpleFluidRenderHandler(PortalCubed.id("block/toxic_goo_still"), PortalCubed.id("block/toxic_goo_flow"))
		);

		ConditionalItemModelProperties.ID_MAPPER.put(PortalCubed.id("lemonade/armed"), Armed.MAP_CODEC);
		RangeSelectItemModelProperties.ID_MAPPER.put(PortalCubed.id("prop_variant"), PropVariantProperty.MAP_CODEC);
		ItemTintSources.ID_MAPPER.put(PortalCubed.id("portal_gun"), PortalGunTintSource.CODEC);

		EntityModelLayerRegistry.registerModelLayer(PortalGunPedestalModel.LAYER_LOCATION, PortalGunPedestalModel::createBodyLayer);
		TerraformBoatClientHelper.registerModelLayers(PortalCubedEntities.LEMON_BOAT);
		PreparableModelLoadingPlugin.register(EmissiveLoader.INSTANCE, PortalCubedModelLoadingPlugin.INSTANCE);
		PortalCubedReloadListeners.registerAssets();

		HudRenderCallback.EVENT.register(SourcePhysics.DebugRenderer.INSTANCE);

		ClientTickEvents.END_CLIENT_TICK.register(ConstructionCannonAnimator::tick);
	}
}
