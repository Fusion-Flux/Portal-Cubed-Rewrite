package io.github.fusionflux.portalcubed.framework.util;

import java.util.Iterator;

import com.google.common.collect.Iterators;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.phys.Vec3;

public record Tri(Vec3 a, Vec3 b, Vec3 c) implements Iterable<Vec3> {
	@Nullable
	public Vec3 clip(Vec3 from, Vec3 to) {
		// implementation from https://iquilezles.org/articles/intersectors/
		Vec3 lineNormal = from.vectorTo(to).normalize();

		Vec3 aToB = this.b.subtract(this.a);
		Vec3 aToC = this.c.subtract(this.a);
		Vec3 aToFrom = from.subtract(this.a);
		Vec3 n = aToB.cross(aToC);
		Vec3 q = aToFrom.cross(lineNormal);
		double d = 1 / lineNormal.dot(n);
		double u = d * q.reverse().dot(aToC);
		double v = d * q.dot(aToB);

		if (u < 0 || v < 0 || (u + v) > 1)
			return null;
		double dist = d * n.reverse().dot(aToFrom);
		return from.add(lineNormal.scale(dist));
	}

	public Vec3 normal() {
		Vec3 a = this.b.subtract(this.a);
		Vec3 b = this.c.subtract(this.a);
		return a.cross(b);
	}

	@NotNull
	@Override
	public Iterator<Vec3> iterator() {
		return Iterators.forArray(this.a, this.b, this.c);
	}
}
