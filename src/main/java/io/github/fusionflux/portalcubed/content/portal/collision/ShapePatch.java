package io.github.fusionflux.portalcubed.content.portal.collision;

import com.google.common.cache.CacheBuilder;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ShapePatch {
	VoxelShape apply(VoxelShape original, EntityCollisionContext ctx);

	/**
	 * A shape patch takes the shape of a block behind a portal, and adds the shape(s) of the corresponding
	 * block(s) on the other side of the portal.
	 * @param state the state on the other side of the portal
	 * @param pos the position of the block on the other side of the portal
	 * @param offset the offset of this patch from the relevant input portal's origin
	 */
	@Nullable
	static ShapePatch create(BlockState state, Level level, BlockPos pos, Vec3 offset) {
		if (state.isAir()) {
			return null;
		} else if (state.getBlock().hasDynamicShape()) {
			return new Dynamic(level, state, pos, offset);
		} else {
			VoxelShape shape = state.getCollisionShape(level, pos);
			// todo:
			// voxelshape
			// rotat e
			return new Static(shape);
		}
	}

	class Static implements ShapePatch {
		private final VoxelShape shape;
		private VoxelShape cached;
		private VoxelShape last;

		public Static(VoxelShape shape) {
			this.shape = shape;
		}

		@Override
		public VoxelShape apply(VoxelShape original, CollisionContext ctx) {
			if (original != last) {
				this.cached = Shapes.or(original, this.shape);
				this.last = original;
			}
			return cached;
		}
	}

    class Dynamic implements ShapePatch {
		private static final Map<Pair<VoxelShape, VoxelShape>, VoxelShape> cache = CacheBuilder.newBuilder()
				.maximumSize(64).<Pair<VoxelShape, VoxelShape>, VoxelShape>build().asMap();

		private final Level level;
        private final BlockState state;
        private final BlockPos pos;
		private final Vec3 offset;

        public Dynamic(Level level, BlockState state, BlockPos pos, Vec3 offset) {
            this.level = level;
            this.state = state;
            this.pos = pos;
			this.offset = offset;
        }

        @Override
        public VoxelShape apply(VoxelShape original, CollisionContext ctx) {
			VoxelShape shape = state.getCollisionShape(level, pos, ctx);
			Pair<VoxelShape, VoxelShape> key = Pair.of(shape, original);
			return cache.computeIfAbsent(key, $ -> {
				// todo:
				// voxelshape
				// rotat e
				return Shapes.or(shape, original);
			});
        }
    }
}
