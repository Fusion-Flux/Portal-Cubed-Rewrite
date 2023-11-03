package io.github.fusionflux.portalcubed.content.portal.collision;

import io.github.fusionflux.portalcubed.framework.extension.LevelExt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

public class CollisionManager {
	private final Level level;
	private final Map<BlockPos, ShapePatch> patches;

	public CollisionManager(Level level) {
		this.level = level;
		this.patches = new HashMap<>();
	}

	public static CollisionManager of(Level level) {
		return ((LevelExt) level).pc$collisionManager();
	}

	public VoxelShape modifyShape(BlockState state, BlockPos pos, VoxelShape original) {
		ShapePatch patch = this.patches.get(pos);
		return patch == null ? original : patch.apply(original);
	}
}
