package io.github.fusionflux.portalcubed.content.portal.placement;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PortalShotClipContextMode {
	public static final String NAME = "PORTALCUBED$PORTAL_SHOT_VISIBLE";

	private static final Supplier<ClipContext.Block> supplier = Suppliers.memoize(() -> ClipContext.Block.valueOf(NAME));

	public static ClipContext.Block get() {
		return supplier.get();
	}

	public static VoxelShape getPortalShotVisibleShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		if (state.is(PortalCubedBlockTags.NONSOLID_TO_PORTAL_SHOTS))
			return Shapes.empty();

		return PortalBumper.getPortalVisibleShape(state, level, pos, ctx);
	}
}
