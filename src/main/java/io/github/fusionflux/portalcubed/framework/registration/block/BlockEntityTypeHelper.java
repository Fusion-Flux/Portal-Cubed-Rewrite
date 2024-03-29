package io.github.fusionflux.portalcubed.framework.registration.block;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityTypeHelper {
	private final Registrar registrar;

	public BlockEntityTypeHelper(Registrar registrar) {
		this.registrar = registrar;
	}

	public <T extends BlockEntity> BlockEntityTypeBuilder<T> create(String name, BlockEntityType.BlockEntitySupplier<T> factory) {
		return new BlockEntityTypeBuilderImpl<>(registrar, name, factory);
	}

	public <T extends BlockEntity> BlockEntityType<T> simple(String name, BlockEntityType.BlockEntitySupplier<T> factory, Block... validBlocks) {
		return create(name, factory).validBlocks(validBlocks).build();
	}
}
