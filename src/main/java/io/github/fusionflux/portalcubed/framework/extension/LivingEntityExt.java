package io.github.fusionflux.portalcubed.framework.extension;

public interface LivingEntityExt {
	default void pc$skipWakeUpMovement(boolean value) {
		throw new AbstractMethodError();
	}
}
