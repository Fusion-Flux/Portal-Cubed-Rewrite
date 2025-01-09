package io.github.fusionflux.portalcubed.framework.registration.item;

import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import net.minecraft.world.item.Item;

public interface ItemFactory<T extends Item> {
	T create(QuiltItemSettings settings);
}
