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

	public static VoxelShape rotateShape(VoxelShape shape, Quaternionf rotation) {
		if (shape.isEmpty())
			return shape;
		DiscreteVoxelShape internal = ((VoxelShapeAccessor) shape).getShape();
		DiscreteVoxelShape newShape = new BitSetDiscreteVoxelShape(SIZE, SIZE, SIZE);
		float width = internal.getXSize() / 2f;
		float height = internal.getYSize() / 2f;
		float depth = internal.getZSize() / 2f;
		for (int x = 0; x < SIZE; x++) {
			float ix = Mth.map(x, 0, SIZE, 0, internal.getXSize()) - width;
			for (int y = 0; y < SIZE; y++) {
				float iy = Mth.map(y, 0, SIZE, 0, internal.getYSize()) - height;
				for (int z = 0; z < SIZE; z++) {
					float iz = Mth.map(z, 0, SIZE, 0, internal.getZSize()) - depth;
					Vector3f rotated = rotation.transform(ix, iy, iz, new Vector3f())
							.add(width, height, depth).round();
					if (rotated.x < 0 || rotated.y < 0 || rotated.z < 0)
						continue;

					if (internal.isFull((int) rotated.x, (int) rotated.y, (int) rotated.z)) {
						newShape.fill(x, y, z);
					}
				}
			}
		}
		return CubeVoxelShapeAccessor.pc$create(newShape);
	}
}
