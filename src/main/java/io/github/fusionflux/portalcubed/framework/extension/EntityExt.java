package io.github.fusionflux.portalcubed.framework.extension;

public interface EntityExt {
	int DISINTEGRATE_TICKS = 3 * 20;

	default boolean pc$disintegrate() { throw new AbstractMethodError(); }
	default boolean pc$disintegrate(int ticks) { throw new AbstractMethodError(); }
	default boolean pc$disintegrating() { throw new AbstractMethodError(); }
	default int pc$disintegrateTicks() { throw new AbstractMethodError(); }
}
