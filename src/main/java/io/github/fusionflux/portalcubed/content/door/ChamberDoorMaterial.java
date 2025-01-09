package io.github.fusionflux.portalcubed.content.door;

import java.util.Locale;
import java.util.function.Supplier;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import io.github.fusionflux.portalcubed.content.panel.PanelMaterial;
import net.minecraft.world.level.material.PushReaction;

public enum ChamberDoorMaterial {
	WHITE(PanelMaterial::whiteSettings),
	METAL(PanelMaterial::p1MetalSettings),
	GRAY(PanelMaterial::smoothGraySettings),
	OLD_AP(PanelMaterial::oldApWhiteSettings);

	public final String name;
	private final QuiltBlockSettings settings;

	ChamberDoorMaterial(Supplier<QuiltBlockSettings> settingsCreator) {
		this.name = this.name().toLowerCase(Locale.ROOT);
		this.settings = settingsCreator.get()
				.nonOpaque()
				.pistonBehavior(PushReaction.DESTROY);
	}

	public QuiltBlockSettings getSettings() {
		return QuiltBlockSettings.copyOf(this.settings);
	}
}
