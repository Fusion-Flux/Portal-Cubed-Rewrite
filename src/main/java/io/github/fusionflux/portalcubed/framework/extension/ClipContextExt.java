package io.github.fusionflux.portalcubed.framework.extension;

public interface ClipContextExt {
	default void pc$setIgnoreInteractionOverride(boolean ignore) {
		throw new AbstractMethodError();
	}

	default boolean pc$ignoreInteractionOverride() {
		throw new AbstractMethodError();
	}
}
