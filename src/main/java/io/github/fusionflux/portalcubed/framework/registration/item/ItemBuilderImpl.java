package io.github.fusionflux.portalcubed.framework.registration.item;

import java.util.Objects;
import java.util.function.Consumer;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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
		}
		return this;
	}

	@Override
	public ItemBuilder<T> group(ResourceKey<CreativeModeTab> key) {
		this.itemGroup = key;
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

		return item;
	}
}
