package io.github.fusionflux.portalcubed.content.misc;

import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MagnesiumFireBlock extends BaseFireBlock {
	public static final MapCodec<MagnesiumFireBlock> CODEC = simpleCodec(MagnesiumFireBlock::new);

	public MagnesiumFireBlock(Properties properties) {
		super(properties, 2);
	}

	@Override
	protected MapCodec<MagnesiumFireBlock> codec() {
		return CODEC;
	}

	@Override
	protected boolean canBurn(BlockState state) {
		return true;
	}

	@Override
	protected BlockState updateShape(
			BlockState state,
			LevelReader level,
			ScheduledTickAccess scheduledTickAccess,
			BlockPos pos,
			Direction direction,
			BlockPos neighborPos,
			BlockState neighborState,
			RandomSource random
	) {
		return this.canSurvive(state, level, pos) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return canSurviveOnBlock(level.getBlockState(pos.below()));
	}

	public static boolean canSurviveOnBlock(BlockState state) {
		return state.is(PortalCubedBlockTags.MAGNESIUM_FIRE_BASE_BLOCKS);
	}
}
