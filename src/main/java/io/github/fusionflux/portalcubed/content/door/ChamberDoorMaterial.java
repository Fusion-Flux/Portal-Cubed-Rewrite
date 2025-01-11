package io.github.fusionflux.portalcubed.content.door;

import java.util.Locale;
import java.util.function.Supplier;

import io.github.fusionflux.portalcubed.content.panel.PanelMaterial;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public enum ChamberDoorMaterial {
	WHITE(PanelMaterial::whiteSettings),
	METAL(PanelMaterial::p1MetalSettings),
	GRAY(PanelMaterial::smoothGraySettings),
	OLD_AP(PanelMaterial::oldApWhiteSettings);

	public final String name;
	private final Supplier<BlockBehaviour.Properties> settings;

	ChamberDoorMaterial(Supplier<BlockBehaviour.Properties> settingsCreator) {
		this.name = this.name().toLowerCase(Locale.ROOT);
		this.settings = settingsCreator;
	}

	public BlockBehaviour.Properties makeProperties() {
		return this.settings.get()
				.noCollission()
				.pushReaction(PushReaction.DESTROY);
	}
}
