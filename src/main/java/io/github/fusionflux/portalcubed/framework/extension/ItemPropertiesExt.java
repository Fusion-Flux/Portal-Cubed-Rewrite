package io.github.fusionflux.portalcubed.framework.extension;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

public interface ItemPropertiesExt {
	/**
	 * Sets the default value of the {@link DataComponents#ITEM_MODEL} component
	 */
	default Item.Properties pc$setModel(@Nullable ResourceLocation id) {
		throw new AbstractMethodError();
	}
}
