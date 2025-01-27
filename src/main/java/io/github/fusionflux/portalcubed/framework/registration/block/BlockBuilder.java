package io.github.fusionflux.portalcubed.framework.registration.block;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.fusionflux.portalcubed.framework.registration.RenderTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public interface BlockBuilder<T extends Block> {
	/**
	 * Make this block copy the settings of the given block.
	 */
	BlockBuilder<T> copyFrom(Block block);

	/**
	 * Set the properties of this block to those provided by the given supplier.
	 * This supplier is expected to return a new Properties instance
	 * each time it is called to avoid problems with mutability.
	 */
	BlockBuilder<T> properties(Supplier<BlockBehaviour.Properties> properties);

	/**
	 * Modify the current properties of this block.
	 */
	BlockBuilder<T> properties(Consumer<BlockBehaviour.Properties> consumer);

	/**
	 * Set the render type of this block.
	 */
	BlockBuilder<T> renderType(RenderTypes type);

	/**
	 * Set the flammability of this block.
	 * @see FireBlock#bootStrap()
	 */
	BlockBuilder<T> flammability(int burn, int spread);

	/**
	 * Set this block as the stripped variant of another.
	 */
	BlockBuilder<T> strippedOf(Block original);

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
