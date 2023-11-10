package io.github.fusionflux.portalcubed.framework.shape;

import io.github.fusionflux.portalcubed.mixin.CubeVoxelShapeAccessor;
import io.github.fusionflux.portalcubed.mixin.VoxelShapeAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class VoxelShenanigans {
	public static final int SIZE = 32;
	/**
	 * This is the max value of the loop below
	 * */
	public static final long MAX = (long) SIZE * (long) SIZE * (long) SIZE - 1;
	public static final long SHIFT = 5;

	public static VoxelShape rotateShape(VoxelShape shape, Quaternionf rotation) {
		if (shape.isEmpty())
			return shape;
		DiscreteVoxelShape internal = ((VoxelShapeAccessor) shape).getShape();
		DiscreteVoxelShape newShape = new BitSetDiscreteVoxelShape(SIZE, SIZE, SIZE);
		float width = internal.getXSize() / 2f;
		float height = internal.getYSize() / 2f;
		float depth = internal.getZSize() / 2f;

		// This helps save on allocations. Go cry to oracle if you don't like it
		Vector3f rotated = new Vector3f();

		// Instead of iterating through each coord like a sane person, let's combine all three axes!
		for (long i = 0; i < MAX; i++) {
			// Evil bit shifts to study and commit crimes to
			int x = (int) (i % SIZE);
			int y = (int) ((i >> SHIFT) % SIZE);
			int z = (int) ((i >> (2*SHIFT)) % SIZE);

			float ix = Mth.map(x, 0, SIZE, 0, internal.getXSize()) - width;
			float iy = Mth.map(y, 0, SIZE, 0, internal.getYSize()) - height;
			float iz = Mth.map(z, 0, SIZE, 0, internal.getZSize()) - depth;

			rotation.transform(ix, iy, iz, rotated)
					.add(width, height, depth).round();
			if (rotated.x < 0 || rotated.y < 0 || rotated.z < 0)
				continue;

			if (internal.isFull((int) rotated.x, (int) rotated.y, (int) rotated.z)) {
				newShape.fill(x, y, z);
			}
		}
		return CubeVoxelShapeAccessor.pc$create(newShape);
	}
}
