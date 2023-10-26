package io.github.fusionflux.portalcubed.registration.block;

import net.minecraft.world.item.Item;

import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public interface BlockItemFactory<T> {
	Item create(T block, QuiltItemSettings settings);
}
