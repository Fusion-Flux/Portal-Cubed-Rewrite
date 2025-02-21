package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationRenderer;
import io.github.fusionflux.portalcubed.framework.render.SimpleBufferSource;

public interface RenderBuffersExt {
	SimpleBufferSource pc$crossPortalBufferSource();

	DisintegrationRenderer.BufferSource pc$disintegratingBufferSource();
	DisintegrationRenderer.BufferSource pc$disintegratingEmissiveBufferSource();
}
