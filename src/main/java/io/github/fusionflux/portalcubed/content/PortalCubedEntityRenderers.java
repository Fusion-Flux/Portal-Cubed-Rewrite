package io.github.fusionflux.portalcubed.content;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

public final class PortalCubedEntityRenderers {
	public static void init() {
		EntityRenderers.register(PortalCubedEntities.LEMON_BOAT, boat(PortalCubedModelLayers.LEMON_BOAT));
		EntityRenderers.register(PortalCubedEntities.LEMON_CHEST_BOAT, boat(PortalCubedModelLayers.LEMON_CHEST_BOAT));
	}

	private static EntityRendererProvider<AbstractBoat> boat(ModelLayerLocation layer) {
		return context -> new BoatRenderer(context, layer);
	}
}
