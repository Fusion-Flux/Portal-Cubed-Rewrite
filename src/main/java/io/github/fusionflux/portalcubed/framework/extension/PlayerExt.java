package io.github.fusionflux.portalcubed.framework.extension;

import java.util.OptionalInt;

public interface PlayerExt {
	void pc$heldProp(OptionalInt prop);
	OptionalInt pc$heldProp();

	void pc$grabSoundTimer(int timer);
	int pc$grabSoundTimer();
	void pc$grabSound(Object grabSound);
	Object pc$grabSound();
	void pc$holdLoopSound(Object holdLoopSound);
	Object pc$holdLoopSound();
}
