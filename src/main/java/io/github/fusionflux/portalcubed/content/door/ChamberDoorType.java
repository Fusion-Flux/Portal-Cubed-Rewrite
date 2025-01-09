package io.github.fusionflux.portalcubed.content.door;

import java.util.List;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import io.github.fusionflux.portalcubed.framework.registration.block.BlockFactory;

public enum ChamberDoorType {
	PORTAL_1("portal_1_chamber_door", ChamberDoorBlock::p1, ChamberDoorMaterial.WHITE, ChamberDoorMaterial.METAL),
	NORMAL("chamber_door", LockingChamberDoorBlock::new),
	OLD_AP("chamber_door", ChamberDoorBlock::oldAp, ChamberDoorMaterial.OLD_AP),
	OCTOPUS("octopus_chamber_door", LockingChamberDoorBlock::new);

	public final String name;
	public final List<ChamberDoorMaterial> materials;
	private final BlockFactory<ChamberDoorBlock> factory;

	ChamberDoorType(String name, BlockFactory<ChamberDoorBlock> factory, ChamberDoorMaterial... materials) {
		this.name = name;
		this.materials = List.of(materials);
		this.factory = factory;
	}

	ChamberDoorType(String name, BlockFactory<ChamberDoorBlock> factory) {
		this(name, factory, ChamberDoorMaterial.WHITE, ChamberDoorMaterial.GRAY);
	}

	public ChamberDoorBlock createBlock(QuiltBlockSettings settings) {
		return this.factory.create(settings);
	}
}
