package io.github.fusionflux.portalcubed.framework.registration.item;

import net.minecraft.world.item.Item;

import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public interface ItemFactory<T extends Item> {
	T create(QuiltItemSettings settings);
}
