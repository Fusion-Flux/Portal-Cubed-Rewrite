package io.github.fusionflux.portalcubed.framework.registration.item;

import java.util.function.Consumer;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public interface ItemBuilder<T extends Item> {
	/**
	 * Set the properties of this item.
	 */
	ItemBuilder<T> properties(Item.Properties properties);

	/**
	 * Modify the properties of this item.
	 * This cannot be used on the same instance as {@link #properties(Properties)}.
	 */
	ItemBuilder<T> properties(Consumer<Item.Properties> consumer);

	/**
	 * Add this item to the given item group.
	 */
	ItemBuilder<T> group(ResourceKey<CreativeModeTab> key);

	/**
	 * Set the compost chance of this item when used on a composter.
	 */
	ItemBuilder<T> compostChance(double chance);

	/**
	 * Build this builder into an item.
	 */
	T build();
}
