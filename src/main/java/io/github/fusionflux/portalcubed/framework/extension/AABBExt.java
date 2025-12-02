package io.github.fusionflux.portalcubed.framework.extension;

import org.joml.Vector3dc;

import net.minecraft.world.phys.AABB;

public interface AABBExt {
	// this value is lazily computed and cached.
	// use a custom interface to ensure a unique descriptor to avoid conflicts
	default Vertices vertices() {
		throw new AbstractMethodError();
	}

	/**
	 * The vertices of an {@link AABB}. The returned vector is reused internally and should not be retained.
	 */
	interface Vertices extends Iterable<Vector3dc> {
	}
}
