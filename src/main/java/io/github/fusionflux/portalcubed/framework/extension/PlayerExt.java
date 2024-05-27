package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;

import org.jetbrains.annotations.Nullable;

public interface PlayerExt {
	// note: no prefixes needed, descriptors guaranteed unique by HoldableEntity
	default void setHeldEntity(HoldableEntity heldEntity) {
		throw new AbstractMethodError();
	}

	@Nullable
	default HoldableEntity getHeldEntity() {
		throw new AbstractMethodError();
	}

	void pc$grabSoundTimer(int timer);
	int pc$grabSoundTimer();
	void pc$grabSound(Object grabSound);
	Object pc$grabSound();
	void pc$holdLoopSound(Object holdLoopSound);
	Object pc$holdLoopSound();
}
