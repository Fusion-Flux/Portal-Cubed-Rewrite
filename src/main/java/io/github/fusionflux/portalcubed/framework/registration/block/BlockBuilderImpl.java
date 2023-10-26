package io.github.fusionflux.portalcubed.framework.registration.block;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import io.github.fusionflux.portalcubed.framework.registration.RenderTypes;
import io.github.fusionflux.portalcubed.framework.registration.item.ItemBuilder;
import net.fabricmc.api.EnvType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

import java.util.function.Consumer;

public class BlockBuilderImpl<T extends Block> implements BlockBuilder<T> {
	private final Registrar registrar;
	private final String name;
	private final BlockFactory<T> factory;

	// mutable properties
	private QuiltBlockSettings settings;
	@Nullable
	private RenderTypes renderType;
	@Nullable
	private BlockItemProvider<T> itemProvider = BlockBuilderImpl::defaultBlock;

	public BlockBuilderImpl(Registrar registrar, String name, BlockFactory<T> factory) {
		this.registrar = registrar;
		this.name = name;
		this.factory = factory;
	}

	@Override
	public BlockBuilder<T> copyFrom(Block block) {
		this.settings = QuiltBlockSettings.copyOf(block);
		this.renderType = registrar.blocks.renderTypes.get(block);
		return this;
	}

	@Override
	public BlockBuilder<T> settings(QuiltBlockSettings settings) {
		this.settings = QuiltBlockSettings.copyOf(settings);
		return this;
	}

	@Override
	public BlockBuilder<T> settings(Consumer<QuiltBlockSettings> consumer) {
		checkSettings();
		consumer.accept(this.settings);
		return this;
	}

	@Override
	public BlockBuilder<T> renderType(RenderTypes type) {
		this.renderType = type;
		return this;
	}

	@Override
	public BlockBuilder<T> item(BlockItemProvider<T> provider) {
		this.itemProvider = provider;
		return this;
	}

	@Override
	public T build() {
		checkSettings();
		T block = this.factory.create(this.settings);
		ResourceLocation id = new ResourceLocation(registrar.modId, this.name);
		Registry.register(BuiltInRegistries.BLOCK, id, block);

		Item item = null;
		if (this.itemProvider != null) {
			ItemBuilder<Item> itemBuilder = registrar.items.create(this.name, settings -> new BlockItem(block, settings));
			ItemBuilder<Item> modifiedBuilder = this.itemProvider.create(block, itemBuilder);
			if (modifiedBuilder != null) {
				item = modifiedBuilder.build();
				Registry.register(BuiltInRegistries.ITEM, id, item);
			}
		}

		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			buildClient(block, item);
		}

		if (this.renderType != null) {
			registrar.blocks.renderTypes.put(block, renderType);
		}

		return block;
	}

	// internal utils

	@ClientOnly
	private void buildClient(Block block, @Nullable Item item) {
		if (this.renderType != null) {
			BlockRenderLayerMap.put(this.renderType.vanilla(), block);
		}
	}

	private void checkSettings() {
		if (this.settings == null) {
			this.settings = QuiltBlockSettings.create();
		}
	}

	private static ItemBuilder<Item> defaultBlock(Block block, ItemBuilder<Item> builder) {
		return builder;
	}
}
