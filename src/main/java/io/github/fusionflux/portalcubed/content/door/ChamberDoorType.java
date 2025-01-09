package io.github.fusionflux.portalcubed.content.door;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import io.github.fusionflux.portalcubed.framework.registration.block.BlockFactory;

public enum ChamberDoorType {
	NORMAL("chamber_door", UnlockingChamberDoorBlock::new),
	OLD_AP("chamber_door", ChamberDoorBlock::oldAp),
	OCTOPUS("octopus_chamber_door", UnlockingChamberDoorBlock::new),
	PORTAL_1("portal_1_chamber_door", ChamberDoorBlock::p1);

	public final String name;
	private final BlockFactory<ChamberDoorBlock> factory;

	ChamberDoorType(String name, BlockFactory<ChamberDoorBlock> factory) {
		this.name = name;
		this.factory = factory;
	}

	public ChamberDoorBlock createBlock(QuiltBlockSettings settings) {
		return this.factory.create(settings);
	}
}
