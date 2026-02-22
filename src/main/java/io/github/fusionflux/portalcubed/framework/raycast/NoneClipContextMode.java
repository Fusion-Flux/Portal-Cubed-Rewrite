package io.github.fusionflux.portalcubed.framework.raycast;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A {@link ClipContext.Block} mode that hits nothing. Useful when raycasting for fluids only.
 */
public final class NoneClipContextMode {
	public static final String NAME = "PORTALCUBED$NONE";

	private static final Supplier<ClipContext.Block> supplier = Suppliers.memoize(() -> ClipContext.Block.valueOf(NAME));

	public static ClipContext.Block get() {
		return supplier.get();
	}

	public static VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return Shapes.empty();
	}
}
