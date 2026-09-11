package io.github.fusionflux.portalcubed.framework.registration.block;

import org.jspecify.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.registration.item.ItemBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface BlockItemProvider<B extends Block> {
	@Nullable ItemBuilder<Item> create(String name, B block, ItemBuilder<Item> builder);

	static @Nullable ItemBuilder<Item> noItem(String name, Block block, ItemBuilder<Item> builder) {
		return null;
	}
}
