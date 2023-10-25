package io.github.fusionflux.portalcubed.registration;

import io.github.fusionflux.portalcubed.registration.block.BlockHelper;

public class Registrar {
	public final String modId;
	public final BlockHelper blocks;

	public Registrar(String modId) {
		this.modId = modId;
		this.blocks = new BlockHelper(this);
	}
}
