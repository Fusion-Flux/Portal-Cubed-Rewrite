package io.github.fusionflux.portalcubed.mixin.client;

import java.util.SequencedMap;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationRenderer;
import io.github.fusionflux.portalcubed.framework.extension.RenderBuffersExt;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;

@Mixin(RenderBuffers.class)
public class RenderBuffersMixin implements RenderBuffersExt {
	@Unique
	private DisintegrationRenderer.BufferSource disintegratingBufferSource;
	@Unique
	private DisintegrationRenderer.BufferSource disintegratingEmissiveBufferSource;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CallbackInfo ci, @Local(ordinal = 0) SequencedMap<RenderType, ByteBufferBuilder> buffers) {
		Set<RenderType> renderTypes = buffers.keySet();
		this.disintegratingBufferSource = new DisintegrationRenderer.BufferSource(renderTypes);
		this.disintegratingEmissiveBufferSource = new DisintegrationRenderer.BufferSource(renderTypes);
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
