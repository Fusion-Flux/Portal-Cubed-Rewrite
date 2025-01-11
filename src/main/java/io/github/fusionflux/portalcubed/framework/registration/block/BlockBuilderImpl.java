package io.github.fusionflux.portalcubed.framework.registration.block;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import io.github.fusionflux.portalcubed.framework.registration.RenderTypes;
import io.github.fusionflux.portalcubed.framework.registration.item.ItemBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockBuilderImpl<T extends Block> implements BlockBuilder<T> {
	private final Registrar registrar;
	private final String name;
	private final BlockFactory<T> factory;

	// mutable properties
	private BlockBehaviour.Properties properties;
	@Nullable
	private RenderTypes renderType;
	@Nullable
	private Flammability flammability;
	@Nullable
	private Block unstripped;
	@Nullable
	private BlockItemProvider<T> itemProvider = BlockBuilderImpl::defaultBlock;
	private BlockItemFactory<T> itemFactory = BlockItem::new;

	public BlockBuilderImpl(Registrar registrar, String name, BlockFactory<T> factory) {
		this.registrar = registrar;
		this.name = name;
		this.factory = factory;
	}

	@Override
	public BlockBuilder<T> copyFrom(Block block) {
		this.properties = BlockBehaviour.Properties.ofFullCopy(block);
		this.renderType = this.registrar.blocks.renderTypes.get(block);
		return this;
	}

	@Override
	public BlockBuilder<T> properties(Supplier<BlockBehaviour.Properties> properties) {
		this.properties = properties.get();
		return this;
	}

	@Override
	public BlockBuilder<T> properties(Consumer<BlockBehaviour.Properties> consumer) {
		this.checkProperties();
		consumer.accept(this.properties);
		return this;
	}

	@Override
	public BlockBuilder<T> renderType(RenderTypes type) {
		this.renderType = type;
		return this;
	}

	@Override
	public BlockBuilder<T> flammability(int burn, int spread) {
		this.flammability = new Flammability(burn, spread);
		return this;
	}

	@Override
	public BlockBuilder<T> strippedOf(Block original) {
		this.unstripped = original;
		return this;
	}

	@Override
	public BlockBuilder<T> item(BlockItemProvider<T> provider) {
		this.itemProvider = provider;
		return this;
	}

	@Override
	public BlockBuilder<T> item(BlockItemFactory<T> factory) {
		this.itemFactory = factory;
		return this;
	}

	@Override
	public T build() {
		this.checkProperties();
		ResourceLocation id = this.registrar.id(this.name);
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
		this.properties.setId(key);
		T block = this.factory.create(this.properties);

		Registry.register(BuiltInRegistries.BLOCK, id, block);

		if (this.flammability != null) {
			FlammableBlockRegistry.getDefaultInstance().add(block, this.flammability.burn, this.flammability.spread);
		}

		if (this.unstripped != null) {
			StrippableBlockRegistry.register(this.unstripped, block);
		}

		if (this.itemProvider != null) {
			ItemBuilder<Item> itemBuilder = this.registrar.items.create(
					this.name, settings -> this.itemFactory.create(block, settings)
			);
			ItemBuilder<Item> modifiedBuilder = this.itemProvider.create(this.name, block, itemBuilder);
			if (modifiedBuilder != null) {
				modifiedBuilder.build(); // registers the item
			}
		}

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			this.buildClient(block);
		}

		if (this.renderType != null) {
			this.registrar.blocks.renderTypes.put(block, this.renderType);
		}

		return block;
	}

	// internal utils

	@Environment(EnvType.CLIENT)
	private void buildClient(Block block) {
		if (this.renderType != null) {
			BlockRenderLayerMap.INSTANCE.putBlock(block, this.renderType.vanilla());
		}
	}

	private void checkProperties() {
		if (this.properties == null) {
			this.properties = BlockBehaviour.Properties.of();
		}
	}

	private static ItemBuilder<Item> defaultBlock(String name, Block block, ItemBuilder<Item> builder) {
		return builder;
	}

	private record Flammability(int burn, int spread) {
	}
}
