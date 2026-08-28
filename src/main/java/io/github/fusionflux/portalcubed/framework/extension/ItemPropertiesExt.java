package io.github.fusionflux.portalcubed.framework.extension;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public interface ItemPropertiesExt {
	/**
	 * Sets the default value of the {@link DataComponents#ITEM_MODEL} component
	 */
	default Item.Properties pc$setModel(@Nullable Identifier id) {
		throw new AbstractMethodError();
	}
}
