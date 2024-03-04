package io.github.fusionflux.portalcubed.framework.construct;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

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
		BlockPos origin = pos.offset(this.offset);
		this.blocks.forEach((relativePos, info) -> {
			BlockPos absolute = origin.offset(relativePos);
			level.setBlockAndUpdate(absolute, info.state());
			info.maybeNbt().ifPresent(nbt -> {
				BlockEntity be = level.getBlockEntity(absolute);
				if (be != null) {
					be.load(nbt);
				}
			});
		});
	}

}
