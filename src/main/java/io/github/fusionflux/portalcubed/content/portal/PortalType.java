package io.github.fusionflux.portalcubed.content.portal;

public enum PortalType {
	PRIMARY(0xff2492fc),
	SECONDARY(0xffff8e1e);

	public final int defaultColor;

	PortalType(int defaultColor) {
		this.defaultColor = defaultColor;
	}
}
