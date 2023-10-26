package io.github.fusionflux.portalcubed.registration.block;

import io.github.fusionflux.portalcubed.registration.Registrar;
import io.github.fusionflux.portalcubed.registration.RenderTypes;
import net.fabricmc.api.EnvType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class BlockBuilderImpl<T extends Block> implements BlockBuilder<T> {
	private final Registrar registrar;
	private final String name;
	private final BlockFactory<T> factory;

	// mutable properties
	private QuiltBlockSettings settings;
	@Nullable
	private RenderTypes renderType;

	// item stuff
	private QuiltItemSettings itemSettings = new QuiltItemSettings();
	// track the original settings instance for safety checking
	private final QuiltItemSettings originalSettings = itemSettings;
	@Nullable
	private BlockItemFactory<T> itemFactory = BlockItem::new;

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
	public BlockBuilder<T> item(BlockItemFactory<T> factory) {
		this.itemFactory = factory;
		return this;
	}

	@Override
	public BlockBuilder<T> noItem() {
		this.itemFactory = null;
		return this;
	}

	@Override
	public BlockBuilder<T> itemSettings(QuiltItemSettings settings) {
		this.itemSettings = Objects.requireNonNull(settings);
		return this;
	}

	@Override
	public BlockBuilder<T> itemSettings(Consumer<QuiltItemSettings> consumer) {
		if (this.originalSettings != this.itemSettings) {
			throw new IllegalArgumentException("Cannot modify replaced item settings.");
		}
		consumer.accept(this.itemSettings);
		return this;
	}

	@Override
	public T build() {
		checkSettings();
		T block = this.factory.create(this.settings);
		ResourceLocation id = new ResourceLocation(registrar.modId, this.name);
		Registry.register(BuiltInRegistries.BLOCK, id, block);

		Item item = null;
		if (this.itemFactory != null) {
			item = this.itemFactory.create(block, this.itemSettings);
			Registry.register(BuiltInRegistries.ITEM, id, item);
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
}
