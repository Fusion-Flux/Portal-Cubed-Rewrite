package io.github.fusionflux.portalcubed.content.door;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import io.github.fusionflux.portalcubed.content.panel.PanelMaterial;
import net.minecraft.world.level.material.PushReaction;

public enum ChamberDoorMaterial {
	OLD_AP(PanelMaterial::oldApWhiteSettings, ChamberDoorType.OLD_AP),
	METAL(PanelMaterial::p1MetalSettings, ChamberDoorType.PORTAL_1),
	GRAY(PanelMaterial::smoothGraySettings, ChamberDoorType.NORMAL, ChamberDoorType.OCTOPUS),
	WHITE(PanelMaterial::whiteSettings, ChamberDoorType.NORMAL, ChamberDoorType.OCTOPUS, ChamberDoorType.PORTAL_1);

	public final String name;
	public final List<ChamberDoorType> types;

	private final QuiltBlockSettings settings;

	ChamberDoorMaterial(Supplier<QuiltBlockSettings> settingsCreator, ChamberDoorType... types) {
		this.name = this.name().toLowerCase(Locale.ROOT);
		this.types = List.of(types);

		this.settings = settingsCreator.get()
				.nonOpaque()
				.pistonBehavior(PushReaction.DESTROY);
	}

	public QuiltBlockSettings getSettings() {
		return QuiltBlockSettings.copyOf(this.settings);
	}
}
