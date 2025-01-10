package io.github.fusionflux.portalcubed.framework.registration.item;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.loader.api.FabricLoader;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class ItemBuilderImpl<T extends Item> implements ItemBuilder<T> {
	private final Registrar registrar;
	private final String name;
	private final ItemFactory<T> factory;

	// mutable properties
	private Item.Properties properties = new Item.Properties();
	// track the original settings for safety checking
	private final Item.Properties originalProperties = this.properties;

	private ResourceKey<CreativeModeTab> itemGroup;
	@Nullable
	private Float compostChance;
	private Supplier<Supplier<ItemColor>> colorProvider;

	public ItemBuilderImpl(Registrar registrar, String name, ItemFactory<T> factory) {
		this.registrar = registrar;
		this.name = name;
		this.factory = factory;
	}

	@Override
	public ItemBuilder<T> properties(Item.Properties properties) {
		this.properties = Objects.requireNonNull(properties);
		return this;
	}

	@Override
	public ItemBuilder<T> properties(Consumer<Item.Properties> consumer) {
		if (this.originalProperties != this.properties) {
			throw new IllegalArgumentException("Cannot modify replaced item properties.");
		} else {
			consumer.accept(this.properties);
		}
		return this;
	}

	@Override
	public ItemBuilder<T> group(ResourceKey<CreativeModeTab> key) {
		this.itemGroup = key;
		return this;
	}

	@Override
	public ItemBuilder<T> compostChance(double chance) {
		// argument is a double to avoid needing to add an f to the end
		this.compostChance = (float) chance;
		return this;
	}

	@Override
	public ItemBuilder<T> colored(Supplier<Supplier<ItemColor>> colorProvider) {
		this.colorProvider = colorProvider;
		return this;
	}

	@Override
	public T build() {
		ResourceLocation id = this.registrar.id(this.name);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
		this.properties.setId(key);
		T item = this.factory.create(this.properties);

		Registry.register(BuiltInRegistries.ITEM, id, item);

		if (this.itemGroup != null) {
			ItemStack stack = new ItemStack(item);
			ItemGroupEvents.modifyEntriesEvent(this.itemGroup).register(entries -> entries.accept(stack));
		}

		if (this.compostChance != null) {
			CompostingChanceRegistry.INSTANCE.add(item, this.compostChance);
		}

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			this.buildClient(item);
		}

		return item;
	}

	@Environment(EnvType.CLIENT)
	private void buildClient(T item) {
		if (this.colorProvider != null) {
			ColorProviderRegistry.ITEM.register(this.colorProvider.get().get(), item);
		}
	}
}
