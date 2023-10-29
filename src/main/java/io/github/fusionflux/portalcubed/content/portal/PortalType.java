package io.github.fusionflux.portalcubed.content.portal;

public enum PortalType {
	PRIMARY(0x2492fc),
	SECONDARY(0xff8e1e);

	public final int defaultColor;

	PortalType(int defaultColor) {
		this.defaultColor = defaultColor;
	}
}
