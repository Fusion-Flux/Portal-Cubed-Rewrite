package io.github.fusionflux.portalcubed.framework.registration.block;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityTypeHelper {
	private final Registrar registrar;

	public BlockEntityTypeHelper(Registrar registrar) {
		this.registrar = registrar;
	}

	public <T extends BlockEntity> BlockEntityTypeBuilder<T> create(String name, FabricBlockEntityTypeBuilder.Factory<T> factory) {
		return new BlockEntityTypeBuilderImpl<>(this.registrar, name, factory);
	}

	public <T extends BlockEntity> BlockEntityType<T> simple(String name, FabricBlockEntityTypeBuilder.Factory<T> factory, Block... validBlocks) {
		return this.create(name, factory).validBlocks(validBlocks).build();
	}
}
