package io.github.fusionflux.portalcubed.framework.block;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.mixin.MultifaceBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class SimpleMultifaceBlock extends MultifaceBlock {
	public static final MapCodec<SimpleMultifaceBlock> CODEC = simpleCodec(SimpleMultifaceBlock::new);

	public SimpleMultifaceBlock(Properties properties) {
		super(properties);
	}

	@Override
	@NotNull
	protected MapCodec<? extends MultifaceBlock> codec() {
		return CODEC;
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (state.getValue(WATERLOGGED))
			scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));

		if (!hasAnyFace(state)) {
			return Blocks.AIR.defaultBlockState();
		} else {
			boolean faceRemoved = hasFace(state, direction) && !canAttachTo(world, direction, neighborPos, neighborState);
			if (faceRemoved) {
				BlockState newState = MultifaceBlockAccessor.callRemoveFace(state, getFaceProperty(direction));
				if (hasAnyFace(newState)) {
					BlockState fakeDropState = this.defaultBlockState()
							.setValue(getFaceProperty(direction), true);
					Block.dropResources(fakeDropState, world, pos, null);
				}
				return newState;
			}
			return state;
		}
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		return context.getItemInHand().is(asItem()) && super.canBeReplaced(state, context);
	}
}
