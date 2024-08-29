package io.github.fusionflux.portalcubed.content.portal.gun_pedestal;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

import static io.github.fusionflux.portalcubed.content.PortalCubedBlocks.PORTAL_GUN_PEDESTAL_ARM;
import static io.github.fusionflux.portalcubed.content.portal.gun_pedestal.PortalGunPedestalArmBlock.HALF;

public class PortalGunPedestalBlock extends Block {
	public static final MapCodec<PortalGunPedestalBlock> CODEC = simpleCodec(PortalGunPedestalBlock::new);
	public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;

	public PortalGunPedestalBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(EXTENDED, false));
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!world.isClientSide()) {
			if (!state.getValue(EXTENDED)) {
				world.destroyBlock(pos.above(), true);
				world.destroyBlock(pos.above(2), true);
				world.setBlock(pos.above(), PORTAL_GUN_PEDESTAL_ARM.defaultBlockState(), UPDATE_ALL);
				world.setBlock(pos.above(2), PORTAL_GUN_PEDESTAL_ARM.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER), UPDATE_ALL);
				world.setBlock(pos, state.setValue(EXTENDED, true), UPDATE_ALL);
			} else {
				if(world.getBlockState(pos.above()).getBlock() instanceof PortalGunPedestalArmBlock) {
					world.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), UPDATE_ALL);
				}
				if(world.getBlockState(pos.above(2)).getBlock() instanceof PortalGunPedestalArmBlock) {
					world.setBlock(pos.above(2), Blocks.AIR.defaultBlockState(), UPDATE_ALL);
				}
				world.setBlock(pos, state.setValue(EXTENDED, false), UPDATE_ALL);
			}

		}

		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		if(world.getBlockState(pos.above()).getBlock() instanceof PortalGunPedestalArmBlock) {
			world.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), UPDATE_ALL);
		}
		if(world.getBlockState(pos.above(2)).getBlock() instanceof PortalGunPedestalArmBlock) {
			world.setBlock(pos.above(2), Blocks.AIR.defaultBlockState(), UPDATE_ALL);
		}
		return super.playerWillDestroy(world, pos, state, player);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(EXTENDED);
	}
}
