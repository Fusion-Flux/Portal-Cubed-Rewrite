package io.github.fusionflux.portalcubed.framework.extension;

import org.joml.Vector3dc;

import net.minecraft.world.phys.Vec3;

public interface Vec3Ext {
	// this value is lazily computed and cached.
	// this should be safe to inject, since any conflict should be functionally identical.
	default Vector3dc asJoml() {
		throw new AbstractMethodError();
	}

	static Vec3 of(Vector3dc vec) {
		return new Vec3(vec.x(), vec.y(), vec.z());
	}
}
