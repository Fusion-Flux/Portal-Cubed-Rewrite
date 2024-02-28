package io.github.fusionflux.portalcubed.content.cannon.screen;

import io.github.fusionflux.portalcubed.content.cannon.data.CannonSettings;

import java.util.function.UnaryOperator;

public class CannonDataHolder {
	private CannonSettings settings;

	public CannonDataHolder(CannonSettings settings) {
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
