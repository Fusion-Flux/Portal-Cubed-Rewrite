package io.github.fusionflux.portalcubed.mixin.utils;

import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import io.github.fusionflux.portalcubed.framework.extension.AABBExt;
import io.github.fusionflux.portalcubed.framework.util.SimpleIterator;
import net.minecraft.world.phys.AABB;

@Mixin(AABB.class)
public class AABBMixin implements AABBExt {
	@Shadow
	@Final
	public double minX;
	@Shadow
	@Final
	public double minY;
	@Shadow
	@Final
	public double minZ;
	@Shadow
	@Final
	public double maxX;
	@Shadow
	@Final
	public double maxY;
	@Shadow
	@Final
	public double maxZ;

	@Unique
	private Vertices vertices;

	@Override
	public Vertices vertices() {
		// another safe technically-a-race-condition
		if (this.vertices == null) {
			this.vertices = () -> {
				// make sure each iterator gets its own scratch vector, since
				// theoretically one AABB can be used across multiple threads
				Vector3d scratch = new Vector3d();
				return SimpleIterator.create(i -> switch (i) {
					case 0 -> scratch.set(this.minX, this.minY, this.minZ);
					case 1 -> scratch.set(this.minX, this.minY, this.maxZ);
					case 2 -> scratch.set(this.minX, this.maxY, this.minZ);
					case 3 -> scratch.set(this.minX, this.maxY, this.maxZ);
					case 4 -> scratch.set(this.maxX, this.minY, this.minZ);
					case 5 -> scratch.set(this.maxX, this.minY, this.maxZ);
					case 6 -> scratch.set(this.maxX, this.maxY, this.minZ);
					case 7 -> scratch.set(this.maxX, this.maxY, this.maxZ);
					default -> null;
				});
			};
		}

		return this.vertices;
	}
}
