package io.github.fusionflux.portalcubed.framework.registration.item;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public interface ItemBuilder<T extends Item> {
	/**
	 * Set the settings of this item.
	 */
	ItemBuilder<T> settings(QuiltItemSettings settings);

	/**
	 * Modify the settings of this item.
	 * This cannot be used on the same instance as {@link #settings(QuiltItemSettings)}.
	 */
	ItemBuilder<T> settings(Consumer<QuiltItemSettings> consumer);

	/**
	 * Add this item to the given item group.
	 */
	ItemBuilder<T> group(ResourceKey<CreativeModeTab> key);

	/**
	 * Register a color provider for this item.
	 */
	ItemBuilder<T> colored(Supplier<Supplier<ItemColor>> colorProvider);

	/**
	 * Build this builder into an item.
	 */
	T build();
}
