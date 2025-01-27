package io.github.fusionflux.portalcubed.framework.extension;

public interface RequiredArgumentBuilderExt {
	default boolean pc$isOptional() {
		throw new AbstractMethodError();
	}

	default void pc$setOptional(boolean value) {
		throw new AbstractMethodError();
	}
}
