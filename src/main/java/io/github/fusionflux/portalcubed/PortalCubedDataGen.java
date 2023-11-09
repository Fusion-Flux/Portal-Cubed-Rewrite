package io.github.fusionflux.portalcubed;

import io.github.fusionflux.portalcubed.data.models.PortalCubedModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class PortalCubedDataGen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		var pack = generator.createPack();
		pack.addProvider(PortalCubedModelProvider::new);
	}
}
