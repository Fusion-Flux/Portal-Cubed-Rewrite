package io.github.fusionflux.portalcubed;

import io.github.fusionflux.portalcubed.content.PortalCubedClientCommands;
import io.github.fusionflux.portalcubed.content.PortalCubedFluids;
import io.github.fusionflux.portalcubed.content.PortalCubedKeyMappings;
import io.github.fusionflux.portalcubed.content.cannon.ConstructPreviewRenderer;
import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonAnimator;
import io.github.fusionflux.portalcubed.content.lemon.Armed;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunModel;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunTintSource;
import io.github.fusionflux.portalcubed.content.prop.renderer.PropVariantProperty;
import io.github.fusionflux.portalcubed.framework.entity.EntityDebugRendering;
import io.github.fusionflux.portalcubed.framework.model.PortalCubedModelLoadingPlugin;
import io.github.fusionflux.portalcubed.framework.render.debug.CameraRotator;
import io.github.fusionflux.portalcubed.framework.util.ClientTicks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.resources.model.sprite.Material;

public class PortalCubedClient implements ClientModInitializer {
	@SuppressWarnings("StaticNonFinalField")
	public static boolean portalDebugEnabled = false;

	@Override
	public void onInitializeClient() {
		// TODO: PORTAL RENDIENRFNSDQIZG - Max
//		PortalRenderer.init();
		EntityDebugRendering.init();
		ConstructPreviewRenderer.init();
		PortalCubedKeyMappings.init();
		PortalCubedClientCommands.init();

		FluidRenderingRegistry.register(
				PortalCubedFluids.GOO,
				PortalCubedFluids.FLOWING_GOO,
				new FluidModel.Unbaked(
						new Material(PortalCubed.id("block/toxic_goo_still")),
						new Material(PortalCubed.id("block/toxic_goo_flow")),
						null, null
				)
		);

		ConditionalItemModelProperties.ID_MAPPER.put(PortalCubed.id("lemonade/armed"), Armed.MAP_CODEC);
		RangeSelectItemModelProperties.ID_MAPPER.put(PortalCubed.id("prop_variant"), PropVariantProperty.MAP_CODEC);
		ItemModels.ID_MAPPER.put(PortalCubed.id("portal_gun"), PortalGunModel.Unbaked.CODEC);
		ItemTintSources.ID_MAPPER.put(PortalCubed.id("portal_gun"), PortalGunTintSource.CODEC);

//		TerraformBoatClientHelper.registerModelLayers(PortalCubedEntities.LEMON_BOAT);
		ModelLoadingPlugin.register(PortalCubedModelLoadingPlugin.INSTANCE);

		ClientTickEvents.END_CLIENT_TICK.register(ClientTicks::tick);
		ClientTickEvents.END_CLIENT_TICK.register(ConstructionCannonAnimator::tick);
		ClientTickEvents.END_CLIENT_TICK.register(CameraRotator::tick);
	}
}
