package io.github.fusionflux.portalcubed.content.cannon.screen;

import java.util.function.UnaryOperator;

import io.github.fusionflux.portalcubed.content.cannon.CannonSettings;

public class CannonSettingsHolder {
	private CannonSettings settings;

	public CannonSettingsHolder(CannonSettings settings) {
		this.settings = settings;
	}

	public CannonSettings get() {
		return this.settings;
	}

	public void set(CannonSettings settings) {
		this.settings = settings;
	}

	public void update(UnaryOperator<CannonSettings> function) {
		this.set(function.apply(this.get()));
	}
}
