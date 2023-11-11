package io.github.fusionflux.portalcubed.framework.shape;

import io.github.fusionflux.portalcubed.mixin.CubeVoxelShapeAccessor;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class VoxelShenanigans {
	/**
	 * The number of voxels on each axis.
	 * It must be within 2^n or else the math will die,
	 * and I have no clue what demons will traverse the portal to hell you'll open.
	 * */
	public static final int RESOLUTION = 16;
	/**
	 * This is the max value of the loop below
	 * */
	public static final long MAX = (long) RESOLUTION * (long) RESOLUTION * (long) RESOLUTION - 1;
	/**
	 * The number of bits required to store {@link VoxelShenanigans#RESOLUTION}
	 * Please keep it in check or society will go up in flames.
	 */
	public static final long SHIFT = 4;

	/**
	 * Rotate a shape by the given quaternion.
	 * Shape is expected to be defined with relative coordinates.
	 */
	public static VoxelShape rotateShape(VoxelShape shape, Quaternionf rotation) {
		if (shape.isEmpty())
			return shape;

		// center shape on 0, 0
		shape = shape.move(-0.5, -0.5, -0.5);
		DiscreteVoxelShape newShape = new BitSetDiscreteVoxelShape(RESOLUTION, RESOLUTION, RESOLUTION);
		FillTest test = FillTest.create(shape, rotation);

		// This helps save on allocations. Go cry to oracle if you don't like it
		Vector3f pos = new Vector3f();
		// Instead of iterating through each coord like a sane person, let's combine all three axes!
		for (long i = 0; i < MAX; i++) {
			// Evil bit shifts to study and commit crimes to
			int x = (int) (i % RESOLUTION);
			int y = (int) ((i >> SHIFT) % RESOLUTION);
			int z = (int) ((i >> (2*SHIFT)) % RESOLUTION);
			// coords are in voxel space
			pos.set(x, y, z);
			// convert to block scale
			pos.mul(1f / RESOLUTION);
			// and offset to origin
			pos.sub(0.5f, 0.5f, 0.5f);

			if (test.isFilled(pos)) {
				newShape.fill(x, y, z);
			}
		}
		return CubeVoxelShapeAccessor.pc$create(newShape);
	}
}
