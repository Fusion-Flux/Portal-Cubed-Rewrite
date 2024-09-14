package io.github.fusionflux.portalcubed.framework.util;

import java.util.Iterator;

import com.google.common.collect.Iterators;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Intersectiond;

import net.minecraft.world.phys.Vec3;

public record Tri(Vec3 a, Vec3 b, Vec3 c) implements Iterable<Vec3> {
	@Nullable
	public Vec3 clip(Vec3 from, Vec3 to) {
		Vec3 direction = from.vectorTo(to).normalize();

		double distance = Intersectiond.intersectRayTriangle(
				from.x, from.y, from.z,
				direction.x, direction.y, direction.z,
				this.a.x, this.a.y, this.a.z,
				this.b.x, this.b.y, this.b.z,
				this.c.x, this.c.y, this.c.z,
				1e-5
		);

		return distance == -1 ? null : from.add(direction.scale(distance));
	}

	public Vec3 normal() {
		Vec3 a = this.b.subtract(this.a);
		Vec3 b = this.c.subtract(this.a);
		return a.cross(b).normalize();
	}

	@NotNull
	@Override
	public Iterator<Vec3> iterator() {
		return Iterators.forArray(this.a, this.b, this.c);
	}
}
