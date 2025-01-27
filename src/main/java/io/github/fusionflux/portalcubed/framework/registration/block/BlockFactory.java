package io.github.fusionflux.portalcubed.framework.registration.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

@FunctionalInterface
public interface BlockFactory<T extends Block> {
	T create(BlockBehaviour.Properties properties);
}
