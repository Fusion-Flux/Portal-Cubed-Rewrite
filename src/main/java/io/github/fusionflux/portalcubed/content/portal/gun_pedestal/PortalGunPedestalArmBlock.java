package io.github.fusionflux.portalcubed.content.portal.gun_pedestal;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import org.jetbrains.annotations.Nullable;

import static io.github.fusionflux.portalcubed.content.portal.gun_pedestal.PortalGunPedestalBlock.EXTENDED;


public class PortalGunPedestalArmBlock extends Block {
	public static final MapCodec<PortalGunPedestalArmBlock> CODEC = simpleCodec(PortalGunPedestalArmBlock::new);
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

	public PortalGunPedestalArmBlock(Properties properties) {
		super(properties);
		this.registerDefaultState((BlockState) this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER);
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		world.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), UPDATE_ALL);
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF);
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		DoubleBlockHalf doubleBlockHalf = state.getValue(HALF);
		if (doubleBlockHalf == DoubleBlockHalf.LOWER) {
			Block upperHalf = world.getBlockState(pos.above()).getBlock();
			Block base = world.getBlockState(pos.below()).getBlock();
			if(upperHalf instanceof PortalGunPedestalArmBlock) {
				world.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), UPDATE_ALL);
			}
			if(base instanceof PortalGunPedestalBlock) {
				world.setBlock(pos.below(), world.getBlockState(pos.below()).setValue(EXTENDED, false), UPDATE_ALL);
			}
		} else if (doubleBlockHalf == DoubleBlockHalf.UPPER) {
			Block lowerHalf = world.getBlockState(pos.below()).getBlock();
			Block base2 = world.getBlockState(pos.below(2)).getBlock();
			if(lowerHalf instanceof PortalGunPedestalArmBlock) {
				world.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), UPDATE_ALL);
			}
			if(base2 instanceof PortalGunPedestalBlock) {
				world.setBlock(pos.below(2), world.getBlockState(pos.below(2)).setValue(EXTENDED, false), UPDATE_ALL);
			}
		}
		return super.playerWillDestroy(world, pos, state, player);
	}

}
