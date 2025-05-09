package io.github.fusionflux.portalcubed.framework.extension;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationRenderer;
import io.github.fusionflux.portalcubed.framework.render.SimpleBufferSource;

public interface RenderBuffersExt {
	SimpleBufferSource pc$crossPortalBufferSource();

	DisintegrationRenderer.BufferSource pc$disintegratingBufferSource();
	DisintegrationRenderer.BufferSource pc$disintegratingEmissiveBufferSource();

	ByteBufferBuilder pc$portalByteBufferBuilder();
	ByteBufferBuilder pc$portalTracerByteBufferBuilder();
}
