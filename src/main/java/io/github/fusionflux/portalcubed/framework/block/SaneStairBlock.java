package io.github.fusionflux.portalcubed.framework.block;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;

public class SaneStairBlock extends StairBlock {
	public SaneStairBlock(Properties settings) {
		super(Blocks.AIR.defaultBlockState(), settings);
	}

	@Override
	public float getExplosionResistance() {
		return this.explosionResistance;
	}
}
