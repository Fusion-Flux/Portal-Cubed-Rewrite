package io.github.fusionflux.portalcubed.framework.registration;

import io.github.fusionflux.portalcubed.framework.registration.block.BlockHelper;
import io.github.fusionflux.portalcubed.framework.registration.entity.EntityHelper;
import io.github.fusionflux.portalcubed.framework.registration.item.ItemHelper;
import net.minecraft.resources.ResourceLocation;

public class Registrar {
	public final String modId;
	public final BlockHelper blocks;
	public final ItemHelper items;
	public final EntityHelper entities;

	public Registrar(String modId) {
		this.modId = modId;
		this.blocks = new BlockHelper(this);
		this.items = new ItemHelper(this);
		this.entities = new EntityHelper(this);
	}

	public ResourceLocation id(String path) {
		return new ResourceLocation(this.modId, path);
	}
}
