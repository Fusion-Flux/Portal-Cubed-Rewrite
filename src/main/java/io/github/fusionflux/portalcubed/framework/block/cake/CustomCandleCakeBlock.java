package io.github.fusionflux.portalcubed.framework.block.cake;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CustomCandleCakeBlock extends CandleCakeBlock {
	public final Block cake;

	public CustomCandleCakeBlock(Block cake, Block candle, Properties settings) {
		super(candle, settings);
		this.cake = cake;
	}

	@Override
	@NotNull
	protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(this.cake);
	}
}
