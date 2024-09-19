package io.github.fusionflux.portalcubed.framework.shape;

import org.jetbrains.annotations.Unmodifiable;

import io.github.fusionflux.portalcubed.mixin.CubeVoxelShapeAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VoxelShenanigans {
	/**
	 * The number of bits required to store {@link VoxelShenanigans#RESOLUTION}
	 */
	public static final long SHIFT = 4;
	/**
	 * The number of voxels on each axis.
	 * Will be 2^{@link VoxelShenanigans#SHIFT}
	 * */
	public static final int RESOLUTION = (int)Math.pow(2, SHIFT);
	/**
	 * This is the max value of the loop below
	 * */
	public static final long MAX = (long) RESOLUTION * (long) RESOLUTION * (long) RESOLUTION;

	/**
	 * Rotate a shape by the given quaternion.
	 * Shape is expected to be defined with relative coordinates.
	 */
//	public static VoxelShape rotateShape(VoxelShape shape, Quaternionf rotation) {
//		if (shape.isEmpty())
//			return shape;
//
//		// center shape on 0, 0
//		shape = shape.move(-0.5, -0.5, -0.5);
//		DiscreteVoxelShape newShape = new BitSetDiscreteVoxelShape(RESOLUTION, RESOLUTION, RESOLUTION);
//		FillTest test = FillTest.create(shape, rotation);
//
//		// This helps save on allocations. Go cry to oracle if you don't like it
//		Vector3f pos = new Vector3f();
//		// Instead of iterating through each coord like a sane person, let's combine all three axes!
//		for (long i = 0; i < MAX; i++) {
//			// Evil bit shifts to study and commit crimes to
//			int x = (int) (i % RESOLUTION);
//			int y = (int) ((i >> SHIFT) % RESOLUTION);
//			int z = (int) ((i >> (2*SHIFT)) % RESOLUTION);
//			// coords are in voxel space
//			pos.set(x, y, z);
//			// convert to block scale
//			pos.mul(1f / RESOLUTION);
//			// and offset to origin
//			pos.sub(0.5f, 0.5f, 0.5f);
//
//			if (test.isFilled(pos)) {
//				newShape.fill(x, y, z);
//			}
//		}
//		return CubeVoxelShapeAccessor.pc$create(newShape);
//	}

//	public static VoxelShape rotateShapeBetween(VoxelShape shape, PortalInstance a, PortalInstance b) {
//		if (shape.isEmpty())
//			return shape;
//		// quaternion multiplication: T * Q applies Q first, then T
//		// want to transform the shape from the front of A to the back of B
//		// transform by inverse of A
//		// transform by inverse of B's 180 rotation
//		// to apply in that order, B * A
//		Quaternionf inverted = a.rotation().invert(new Quaternionf());
//		Quaternionf totalRotation = b.rotation180.mul(inverted, new Quaternionf());
//		return rotateShape(shape, totalRotation);
//	}

//	public static List<VoxelShape> getShapesBehindPortal(Level level, @Nullable Entity entity, PortalInstance portal, PortalInstance linked) {
//		Iterable<VoxelShape> shapes = List.of(); //level.getCollisions(entity, linked.collisionCollectionArea);
//		List<VoxelShape> behindPortal = new ArrayList<>();
//		for (VoxelShape shape : shapes) {
//			// translate to origin
//			Vec3 center = shape.bounds().getCenter();
//			shape = shape.move(-center.x, -center.y, -center.z);
//			// rotate around
//			// rotateShape subtracts an extra 0.5, compensate here
//			shape = shape.move(0.5, 0.5, 0.5);
//			shape = VoxelShenanigans.rotateShapeBetween(shape, linked, portal);
//			shape = shape.move(-0.5, -0.5, -0.5);
//			// de-relativize to other portal
//			shape = shape.move(portal.data.origin().x, portal.data.origin().y, portal.data.origin().z);
//			// translate to right pos
//			Vec3 originToBlock = linked.data.origin().vectorTo(center);
//			Vec3 teleported = PortalTeleportHandler.teleportRelativeVecBetween(originToBlock, linked, portal);
//			shape = shape.move(teleported.x, teleported.y, teleported.z);
//			behindPortal.add(shape);
//		}
//		return behindPortal;
//	}

	@Unmodifiable
	public static Object2ObjectMap<BlockPos, VoxelShape> approximateObb(OBB obb) {
		Object2ObjectMap<BlockPos, VoxelShape> shapes = new Object2ObjectArrayMap<>();
		obb.intersectingBlocks().forEach(pos -> {
			VoxelShape shape = approximateObb(obb, pos)
					.move(pos.getX(), pos.getY(), pos.getZ());
			shapes.put(pos.immutable(), shape.optimize());
		});
		return Object2ObjectMaps.unmodifiable(shapes);
	}

	private static VoxelShape approximateObb(OBB obb, BlockPos pos) {
		final int resolution = 32;
		final float step = 1f / resolution;
		final float toCenter = step / 2;

		BitSetDiscreteVoxelShape shape = new BitSetDiscreteVoxelShape(resolution, resolution, resolution);

		for (int x = 0; x < resolution; x++) {
			for (int y = 0; y < resolution; y++) {
				for (int z = 0; z < resolution; z++) {
					double posX = pos.getX() + (x * step) + toCenter;
					double posY = pos.getY() + (y * step) + toCenter;
					double posZ = pos.getZ() + (z * step) + toCenter;
					if (obb.contains(posX, posY, posZ)) {
						shape.fill(x, y, z);
					}
				}
			}
		}

		return CubeVoxelShapeAccessor.pc$create(shape);
	}
}
