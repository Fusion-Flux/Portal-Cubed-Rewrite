package io.github.fusionflux.portalcubed.framework.registration.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface BlockItemFactory<T extends Block> {
	Item create(T block, Item.Properties properties);
}
