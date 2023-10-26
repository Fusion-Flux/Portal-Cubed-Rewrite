package io.github.fusionflux.portalcubed.registration.item;

import java.util.function.Consumer;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

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
	 * Build this builder into an item.
	 */
	T build();
}
