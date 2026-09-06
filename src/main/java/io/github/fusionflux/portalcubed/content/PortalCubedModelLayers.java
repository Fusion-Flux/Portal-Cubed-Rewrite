package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.TexturedLayerDefinitionProvider;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;

public final class PortalCubedModelLayers {
	public static final ModelLayerLocation LEMON_BOAT = register("boat/lemon", BoatModel::createBoatModel);
	public static final ModelLayerLocation LEMON_CHEST_BOAT = register("chest_boat/lemon", BoatModel::createChestBoatModel);

	public static void init() {
	}

	private static ModelLayerLocation register(String name, TexturedLayerDefinitionProvider provider) {
		ModelLayerLocation layer = new ModelLayerLocation(PortalCubed.id(name), "main");
		ModelLayerRegistry.registerModelLayer(layer, provider);
		return layer;
	}
}
