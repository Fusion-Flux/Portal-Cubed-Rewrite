package io.github.fusionflux.portalcubed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import io.github.fusionflux.portalcubed.framework.extension.RequiredArgumentBuilderExt;


@Mixin(value = RequiredArgumentBuilder.class, remap = false)
public class RequiredArgumentBuilderMixin implements RequiredArgumentBuilderExt {
	@Unique
	private boolean optional;

	@Override
	public boolean pc$isOptional() {
		return this.optional;
	}

	@Override
	public void pc$setOptional(boolean value) {
		this.optional = value;
	}
}
