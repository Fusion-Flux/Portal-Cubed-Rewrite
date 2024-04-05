package io.github.fusionflux.portalcubed.content.misc;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MagnesiumFireBlock extends BaseFireBlock {
	public MagnesiumFireBlock(BlockBehaviour.Properties properties) {
		super(properties, 2.0F);
	}

	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		return this.canSurvive(state, world, pos) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
	}

	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		return false;
	}
	public static boolean canSurviveOnBlock(BlockState state) {
		return state.is(PortalCubedBlockTags.MAGNESIUM_FIRE_BASE_BLOCKS);
	}
	protected boolean canBurn(BlockState state) {
		return true;
	}
}
