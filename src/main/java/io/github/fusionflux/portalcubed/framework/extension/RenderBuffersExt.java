package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationRenderer;

public interface RenderBuffersExt {
	DisintegrationRenderer.BufferSource pc$disintegratingBufferSource();
	DisintegrationRenderer.BufferSource pc$disintegratingEmissiveBufferSource();
}
