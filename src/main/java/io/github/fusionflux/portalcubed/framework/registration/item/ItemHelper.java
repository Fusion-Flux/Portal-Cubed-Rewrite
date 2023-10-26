package io.github.fusionflux.portalcubed.framework.registration.item;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.minecraft.world.item.Item;

public class ItemHelper {
	private final Registrar registrar;

	public ItemHelper(Registrar registrar) {
		this.registrar = registrar;
	}

	public ItemBuilder<Item> create(String name) {
		return create(name, Item::new);
	}

	public <T extends Item> ItemBuilder<T> create(String name, ItemFactory<T> factory) {
		return new ItemBuilderImpl<>(registrar, name, factory);
	}

	public Item simple(String name) {
		return create(name).build();
	}

	public <T extends Item> T simple(String name, ItemFactory<T> factory) {
		return create(name, factory).build();
	}
}
