package io.github.fusionflux.portalcubed.registration.item;

import java.util.Objects;
import java.util.function.Consumer;

import io.github.fusionflux.portalcubed.registration.Registrar;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class ItemBuilderImpl<T extends Item> implements ItemBuilder<T> {
	private final Registrar registrar;
	private final String name;
	private final ItemFactory<T> factory;

	// mutable properties
	private QuiltItemSettings settings = new QuiltItemSettings();
	// track the original settings for safety checking
	private final QuiltItemSettings originalSettings = settings;

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
	public T build() {
		T item = this.factory.create(this.settings);
		ResourceLocation id = new ResourceLocation(registrar.modId, this.name);
		return Registry.register(BuiltInRegistries.ITEM, id, item);
	}
}
