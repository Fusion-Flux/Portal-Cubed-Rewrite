package io.github.fusionflux.portalcubed.framework.construct;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

/**
 * A construct that has been configured with a rotation and offset, ready for placement.
 */
public class ConfiguredConstruct {
	public final Map<BlockPos, Construct.BlockInfo> blocks;
	public final Rotation rotation;
	public final BoundingBox bounds;
	public final Vec3i offset;

	public ConfiguredConstruct(Construct construct) {
		this(construct, Rotation.NONE, Vec3i.ZERO);
	}

	public ConfiguredConstruct(Construct construct, Rotation rotation, Vec3i offset) {
		this.blocks = construct.getBlocks(rotation);
		this.rotation = rotation;
		this.bounds = construct.getBounds(rotation);
		this.offset = offset;
	}

	public BoundingBox getAbsoluteBounds(BlockPos pos) {
		return this.bounds.moved(
				pos.getX() + this.offset.getX(),
				pos.getY() + this.offset.getY(),
				pos.getZ() + this.offset.getZ()
		);
	}

	public void place(ServerLevel level, BlockPos pos) {
		this.getAbsoluteBlocks(pos).forEach((blockPos, info) -> {
			level.setBlockAndUpdate(blockPos, info.state());
			info.maybeNbt().ifPresent(nbt -> {
				BlockEntity be = level.getBlockEntity(blockPos);
				if (be != null) {
					be.load(nbt);
				}
			});
		});
	}

	public boolean isObstructed(Level level, BlockPos pos) {
		for (Map.Entry<BlockPos, Construct.BlockInfo> entry : this.getAbsoluteBlocks(pos).entrySet()) {
			BlockPos blockPos = entry.getKey();
			BlockState existingState = level.getBlockState(blockPos);
			if (!existingState.canBeReplaced())
				return true;

			// check entity collision
			BlockState state = entry.getValue().state();
			VoxelShape shape = state.getCollisionShape(level, blockPos);
			shape = shape.move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
			if (!level.isUnobstructed(null, shape)) {
				return true;
			}
		}
		return false;
	}

	private Map<BlockPos, Construct.BlockInfo> getAbsoluteBlocks(BlockPos pos) {
		Map<BlockPos, Construct.BlockInfo> map = new HashMap<>();

		BlockPos origin = pos.offset(this.offset);
		this.blocks.forEach((relativePos, info) -> {
			BlockPos absolute = origin.offset(relativePos);
			map.put(absolute, info);
		});

		return map;
	}
}
