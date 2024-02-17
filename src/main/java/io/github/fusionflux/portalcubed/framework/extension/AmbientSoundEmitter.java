package io.github.fusionflux.portalcubed.framework.extension;

import org.quiltmc.loader.api.minecraft.ClientOnly;

public interface AmbientSoundEmitter {
	@ClientOnly void playAmbientSound();
}
