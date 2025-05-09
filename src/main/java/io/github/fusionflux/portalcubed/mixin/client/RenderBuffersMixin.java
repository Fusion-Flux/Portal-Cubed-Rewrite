package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationRenderer;
import io.github.fusionflux.portalcubed.framework.extension.RenderBuffersExt;
import io.github.fusionflux.portalcubed.framework.render.SimpleBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;

@Mixin(RenderBuffers.class)
public class RenderBuffersMixin implements RenderBuffersExt {
	@Unique
	private SimpleBufferSource crossPortalBufferSource;
	@Unique
	private DisintegrationRenderer.BufferSource disintegratingBufferSource;
	@Unique
	private DisintegrationRenderer.BufferSource disintegratingEmissiveBufferSource;
	@Unique
	private ByteBufferBuilder portalByteBufferBuilder;
	@Unique
	private ByteBufferBuilder portalTracerByteBufferBuilder;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		this.crossPortalBufferSource = new SimpleBufferSource();
		this.disintegratingBufferSource = new DisintegrationRenderer.BufferSource();
		this.disintegratingEmissiveBufferSource = new DisintegrationRenderer.BufferSource();
		this.portalByteBufferBuilder = new ByteBufferBuilder(RenderType.TRANSIENT_BUFFER_SIZE);
		this.portalTracerByteBufferBuilder = new ByteBufferBuilder(RenderType.TRANSIENT_BUFFER_SIZE);
	}

	@Override
	public SimpleBufferSource pc$crossPortalBufferSource() {
		return this.crossPortalBufferSource;
	}

	@Override
	public DisintegrationRenderer.BufferSource pc$disintegratingBufferSource() {
		return this.disintegratingBufferSource;
	}

	@Override
	public DisintegrationRenderer.BufferSource pc$disintegratingEmissiveBufferSource() {
		return this.disintegratingEmissiveBufferSource;
	}

	@Override
	public ByteBufferBuilder pc$portalByteBufferBuilder() {
		return this.portalByteBufferBuilder;
	}

	@Override
	public ByteBufferBuilder pc$portalTracerByteBufferBuilder() {
		return this.portalTracerByteBufferBuilder;
	}
}
