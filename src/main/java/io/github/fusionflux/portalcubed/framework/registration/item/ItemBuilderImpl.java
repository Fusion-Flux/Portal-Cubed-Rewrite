package io.github.fusionflux.portalcubed.framework.registration.item;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class ItemBuilderImpl<T extends Item> implements ItemBuilder<T> {
	private final Registrar registrar;
	private final String name;
	private final ItemFactory<T> factory;

	// mutable properties
	private QuiltItemSettings settings = new QuiltItemSettings();
	// track the original settings for safety checking
	private final QuiltItemSettings originalSettings = settings;

	private ResourceKey<CreativeModeTab> itemGroup;
	private Supplier<Supplier<ItemColor>> colorProvider;

	public ItemBuilderImpl(Registrar registrar, String name, ItemFactory<T> factory) {
		this.registrar = registrar;
		this.name = name;
		this.factory = factory;
	}

	@Override
	public ItemBuilder<T> settings(QuiltItemSettings settings) {
		this.settings = Objects.requireNonNull(settings);
		return this;
	}

	@Override
	public ItemBuilder<T> settings(Consumer<QuiltItemSettings> consumer) {
		if (this.originalSettings != this.settings) {
			throw new IllegalArgumentException("Cannot modify replaced item settings.");
		} else {
			consumer.accept(this.settings);
		}
		return this;
	}

	@Override
	public ItemBuilder<T> group(ResourceKey<CreativeModeTab> key) {
		this.itemGroup = key;
		return this;
	}

	@Override
	public ItemBuilder<T> colored(Supplier<Supplier<ItemColor>> colorProvider) {
		this.colorProvider = colorProvider;
		return this;
	}

	@Override
	public T build() {
		T item = this.factory.create(this.settings);
		ResourceLocation id = registrar.id(this.name);
		Registry.register(BuiltInRegistries.ITEM, id, item);

		if (this.itemGroup != null) {
			ItemStack stack = new ItemStack(item);
			ItemGroupEvents.modifyEntriesEvent(this.itemGroup).register(entries -> entries.accept(stack));
		}

		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			this.buildClient(item);
		}

		return item;
	}

	@ClientOnly
	private void buildClient(T item) {
		if (this.colorProvider != null) {
			ColorProviderRegistry.ITEM.register(this.colorProvider.get().get(), item);
		}
	}
}
