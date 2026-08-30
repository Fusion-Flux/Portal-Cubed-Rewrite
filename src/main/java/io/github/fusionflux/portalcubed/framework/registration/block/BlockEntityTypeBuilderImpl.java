package io.github.fusionflux.portalcubed.framework.registration.block;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityTypeBuilderImpl<T extends BlockEntity> implements BlockEntityTypeBuilder<T> {
	private final Registrar registrar;
	private final String name;
	private final FabricBlockEntityTypeBuilder<T> typeBuilder;

	public BlockEntityTypeBuilderImpl(Registrar registrar, String name, FabricBlockEntityTypeBuilder.Factory<T> factory) {
		this.registrar = registrar;
		this.name = name;
		this.typeBuilder = FabricBlockEntityTypeBuilder.create(factory);
	}

	@Override
	public BlockEntityTypeBuilder<T> validBlocks(Block... blocks) {
		this.typeBuilder.addBlocks(blocks);
		return this;
	}

	@Override
	public BlockEntityType<T> build() {
		BlockEntityType<T> type = this.typeBuilder.build();
		Identifier id = this.registrar.id(this.name);
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type);
	}
}
