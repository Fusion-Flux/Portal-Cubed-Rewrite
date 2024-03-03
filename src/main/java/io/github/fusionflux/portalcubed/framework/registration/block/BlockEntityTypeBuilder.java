package io.github.fusionflux.portalcubed.framework.registration.block;

import java.util.function.Supplier;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface BlockEntityTypeBuilder<T extends BlockEntity> {
	BlockEntityTypeBuilder<T> validBlocks(Block... blocks);

	BlockEntityTypeBuilder<T> renderer(Supplier<Supplier<BlockEntityRendererProvider<T>>> supplier);

	/**
	 * Build this builder into a block entity type.
	 */
	BlockEntityType<T> build();
}
