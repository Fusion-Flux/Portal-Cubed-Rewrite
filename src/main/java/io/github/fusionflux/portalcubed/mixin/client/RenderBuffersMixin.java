package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationRenderer;
import io.github.fusionflux.portalcubed.framework.extension.RenderBuffersExt;
import io.github.fusionflux.portalcubed.framework.render.SimpleBufferSource;
import net.minecraft.client.renderer.RenderBuffers;

@Mixin(RenderBuffers.class)
public class RenderBuffersMixin implements RenderBuffersExt {
	@Unique
	private SimpleBufferSource crossPortalBufferSource;
	@Unique
	private DisintegrationRenderer.BufferSource disintegratingBufferSource;
	@Unique
	private DisintegrationRenderer.BufferSource disintegratingEmissiveBufferSource;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		this.crossPortalBufferSource = new SimpleBufferSource();
		this.disintegratingBufferSource = new DisintegrationRenderer.BufferSource();
		this.disintegratingEmissiveBufferSource = new DisintegrationRenderer.BufferSource();
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
}
