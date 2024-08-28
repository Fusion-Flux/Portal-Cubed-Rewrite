package io.github.fusionflux.portalcubed.mixin.client;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.fusionflux.portalcubed.framework.extension.BlockElementExt;
import net.minecraft.client.renderer.block.model.BlockElement;

@Mixin(BlockElement.class)
public class BlockElementMixin implements BlockElementExt {
	@Unique
	private String name;

	@Unique
	private BlendMode blendMode;

	@Override
	@Nullable
	public String pc$name() {
		return this.name;
	}

	@Override
	public void pc$setName(String name) {
		this.name = name;
	}

	@Override
	@Nullable
	public BlendMode pc$blendMode() {
		return this.blendMode;
	}

	@Override
	public void pc$setBlendMode(BlendMode mode) {
		this.blendMode = mode;
	}
}
