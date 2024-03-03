package io.github.fusionflux.portalcubed;

import io.github.fusionflux.portalcubed.data.models.PedestalButtonBlockStates;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Datagen Process:
 * <p>
 * Each provider should have a small, defined scope. Add each one to the pack below,
 * and comment the line once finished.
 * </p>
 * <p>
 * Data is to be generated once and then moved to the resources folder.
 * Add a comment to the generator class listing which files it generates.
 * When a provider generates, a cache file is created. This can be used to
 * grab a list of files easily.
 * </p>
 * <p>
 * This is done to get the benefits of datagen, without losing the benefits of
 * manual data creation.
 * </p>
 */
public class PortalCubedDataGen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		var pack = generator.createPack();
//		pack.addProvider(FloorButtonBlockStates::new);
		pack.addProvider(PedestalButtonBlockStates::new);
	}
}
