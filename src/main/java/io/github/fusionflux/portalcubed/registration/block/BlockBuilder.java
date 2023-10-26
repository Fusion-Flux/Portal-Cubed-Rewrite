package io.github.fusionflux.portalcubed.registration.block;

import io.github.fusionflux.portalcubed.registration.RenderTypes;
import io.github.fusionflux.portalcubed.registration.item.ItemBuilder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface BlockBuilder<T extends Block> {
	/**
	 * Make this block copy the settings of the given block.
	 */
	BlockBuilder<T> copyFrom(Block block);

	/**
	 * Set the settings of this block to the given value.
	 */
	BlockBuilder<T> settings(QuiltBlockSettings settings);

	/**
	 * Modify the current settings of this block.
	 */
	BlockBuilder<T> settings(Consumer<QuiltBlockSettings> consumer);

	/**
	 * Set the render type of this block.
	 */
	BlockBuilder<T> renderType(RenderTypes type);

	/**
	 * Set the item for this block.
	 */
	BlockBuilder<T> item(BlockItemFactory<T> factory);

	/**
	 * Do not automatically create an item for this block.
	 */
	BlockBuilder<T> noItem();

	/**
	 * Set the settings of this block's item to the given value.
	 */
	BlockBuilder<T> itemSettings(QuiltItemSettings settings);

	/**
	 * Modify the current settings of this block's item.
	 * This cannot be called on the same instance as {@link #itemSettings(QuiltItemSettings)}
	 */
	BlockBuilder<T> itemSettings(Consumer<QuiltItemSettings> consumer);

	/**
	 * Build this builder into a block.
	 */
	T build();
}
