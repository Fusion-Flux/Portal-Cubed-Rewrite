package io.github.fusionflux.portalcubed.framework.extension;

public interface ClientboundTeleportEntityPacketExt {
	default boolean pc$isLocal() {
		throw new AbstractMethodError();
	}

	default void pc$setLocal(boolean value) {
		throw new AbstractMethodError();
	}
}
