package io.github.fusionflux.portalcubed.registration.block;

import io.github.fusionflux.portalcubed.registration.RenderTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.function.Consumer;

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
	 * Modify the item for this block.
	 */
	BlockBuilder<T> item(BlockItemProvider<T> provider);

	/**
	 * Build this builder into a block.
	 */
	T build();
}
