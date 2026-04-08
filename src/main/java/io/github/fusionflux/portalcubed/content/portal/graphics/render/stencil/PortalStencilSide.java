package io.github.fusionflux.portalcubed.content.portal.graphics.render.stencil;

import io.github.fusionflux.portalcubed.framework.util.SimpleIterator;

public enum PortalStencilSide {
	BOTTOM(
			new Vertex(false, false, true),
			new Vertex(true,  false, true),
			new Vertex(true,  true,  true),
			new Vertex(false, true,  true)
	),
	LEFT(
			new Vertex(false, false, false),
			new Vertex(false, false, true),
			new Vertex(false, true,  true),
			new Vertex(false, true,  false)
	),
	TOP(
			new Vertex(false, false, false),
			new Vertex(false, true,  false),
			new Vertex(true,  true,  false),
			new Vertex(true,  false, false)
	),
	RIGHT(
			new Vertex(true,  false, false),
			new Vertex(true,  true,  false),
			new Vertex(true,  true,  true),
			new Vertex(true,  false, true)
	);

	public final Vertex bottomLeft;
	public final Vertex bottomRight;
	public final Vertex topRight;
	public final Vertex topLeft;

	PortalStencilSide(Vertex bottomLeft, Vertex bottomRight, Vertex topRight, Vertex topLeft) {
		this.bottomLeft = bottomLeft;
		this.bottomRight = bottomRight;
		this.topRight = topRight;
		this.topLeft = topLeft;
	}

	public Iterable<Vertex> vertices() {
		return () -> SimpleIterator.create(i -> switch (i) {
			case 0 -> this.bottomLeft;
			case 1 -> this.bottomRight;
			case 2 -> this.topRight;
			case 3 -> this.topLeft;
			default -> null;
		});
	}

	public record Vertex(boolean x, boolean y, boolean z) { }
}
