package io.github.fusionflux.portalcubed.framework.registration.block;

import java.util.function.Consumer;

import io.github.fusionflux.portalcubed.framework.registration.RenderTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public interface BlockBuilder<T extends Block> {
	/**
	 * Make this block copy the settings of the given block.
	 */
	BlockBuilder<T> copyFrom(Block block);

	/**
	 * Set the properties of this block to the given value.
	 * Settings are copied, it is safe to re-use the same instance.
	 */
	BlockBuilder<T> properties(BlockBehaviour.Properties properties);

	/**
	 * Modify the current properties of this block.
	 */
	BlockBuilder<T> properties(Consumer<BlockBehaviour.Properties> consumer);

	/**
	 * Set the render type of this block.
	 */
	BlockBuilder<T> renderType(RenderTypes type);

	/**
	 * Modify the item for this block.
	 */
	BlockBuilder<T> item(BlockItemProvider<T> provider);

	/**
	 * Set the factory for this block's item.
	 */
	BlockBuilder<T> item(BlockItemFactory<T> factory);

	/**
	 * Build this builder into a block.
	 */
	T build();
}
