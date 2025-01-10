package io.github.fusionflux.portalcubed.framework.registration.block;

import java.util.function.Supplier;

import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityTypeBuilderImpl<T extends BlockEntity> implements BlockEntityTypeBuilder<T> {
	private final Registrar registrar;
	private final String name;
	private final FabricBlockEntityTypeBuilder<T> typeBuilder;

	private Supplier<Supplier<BlockEntityRendererProvider<T>>> rendererSupplier;

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
	public BlockEntityTypeBuilder<T> renderer(Supplier<Supplier<BlockEntityRendererProvider<T>>> supplier) {
		this.rendererSupplier = supplier;
		return this;
	}

	@Override
	public BlockEntityType<T> build() {
		BlockEntityType<T> type = this.typeBuilder.build();
		ResourceLocation id = this.registrar.id(this.name);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type);

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			this.buildClient(type);
		}

		return type;
	}

	@Environment(EnvType.CLIENT)
	private void buildClient(BlockEntityType<T> type) {
		if (this.rendererSupplier != null) {
			BlockEntityRenderers.register(type, this.rendererSupplier.get().get());
		}
	}
}
