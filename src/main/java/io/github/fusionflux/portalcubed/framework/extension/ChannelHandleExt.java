package io.github.fusionflux.portalcubed.framework.extension;

public interface ChannelHandleExt {
	default boolean pc$teleportedLastTick() {
		throw new AbstractMethodError();
	}

	default void pc$setTeleportedLastTick(boolean value) {
		throw new AbstractMethodError();
	}
}
