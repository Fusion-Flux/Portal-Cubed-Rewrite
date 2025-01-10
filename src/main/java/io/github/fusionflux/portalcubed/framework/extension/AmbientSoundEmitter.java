package io.github.fusionflux.portalcubed.framework.extension;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface AmbientSoundEmitter {
	@Environment(EnvType.CLIENT)
	void playAmbientSound();
}
