package io.github.fusionflux.portalcubed.framework.registration.block;

import io.github.fusionflux.portalcubed.framework.registration.item.ItemBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface BlockItemProvider<B extends Block> {
	ItemBuilder<Item> create(String name, B block, ItemBuilder<Item> builder);

	static ItemBuilder<Item> noItem(String name, Block block, ItemBuilder<Item> builder) {
		return null;
	}
}
