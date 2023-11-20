package io.github.fusionflux.portalcubed.content.portal.collision;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShapePatch {
	private final CollisionManager manager;
	private final Level level;
	private final BlockState state;
	private final BlockPos pos;
	private final Cache<ShapeKey, VoxelShape> cache;

	public ShapePatch(CollisionManager manager, Level level, BlockState state, BlockPos pos) {
		this.manager = manager;
		this.level = level;
		this.state = state;
		this.pos = pos;
		this.cache = CacheBuilder.newBuilder().maximumSize(8).build();
	}

	public VoxelShape apply(VoxelShape original, EntityCollisionContext ctx) {
//		VoxelShape shape = state.getCollisionShape(level, pos, ctx);
//		ShapeKey key = new ShapeKey(original, shape);
//		return cache.asMap().computeIfAbsent(key, $ -> {
			return manager.modifyShapeForPortals(original, pos, ctx.getEntity());
//		});
	}

	private record ShapeKey(VoxelShape original, VoxelShape shape) {
	}
}
