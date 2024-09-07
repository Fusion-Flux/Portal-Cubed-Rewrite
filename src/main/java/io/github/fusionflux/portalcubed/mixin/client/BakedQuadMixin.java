package io.github.fusionflux.portalcubed.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.fusionflux.portalcubed.framework.extension.BakedQuadExt;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.renderer.block.model.BakedQuad;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements BakedQuadExt {
	@Unique
	private BlendMode blendMode;

	@Unique
	private String textureReference;

	@Override
	@Nullable
	public BlendMode pc$blendMode() {
		return this.blendMode;
	}

	@Override
	public void pc$setBlendMode(BlendMode mode) {
		this.blendMode = mode;
	}

	@Override
	@Nullable
	public String pc$textureReference() {
		return this.textureReference;
	}

	@Override
	public void pc$setTextureReference(String textureReference) {
		this.textureReference = textureReference;
	}
}
