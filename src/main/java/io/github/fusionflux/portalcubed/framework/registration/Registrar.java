package io.github.fusionflux.portalcubed.framework.registration;

import io.github.fusionflux.portalcubed.framework.registration.block.BlockHelper;
import io.github.fusionflux.portalcubed.framework.registration.item.ItemHelper;

public class Registrar {
	public final String modId;
	public final BlockHelper blocks;
	public final ItemHelper items;

	public Registrar(String modId) {
		this.modId = modId;
		this.blocks = new BlockHelper(this);
		this.items = new ItemHelper(this);
	}
}
