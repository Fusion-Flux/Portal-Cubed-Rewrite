package io.github.fusionflux.portalcubed.framework.block.cake;

import java.util.Map;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;

public class CustomCakeBlock extends CakeBlock {
	private final Map<Block, Block> candleToCake;

	public CustomCakeBlock(Properties properties, Map<Block, Block> candleToCake) {
		super(properties);
		this.candleToCake = candleToCake;
	}

	public Block getWithCandle(Block candle) {
		return this.candleToCake.getOrDefault(candle, Blocks.AIR);
	}
}
