package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.math.Transformation;

import io.github.fusionflux.portalcubed.framework.extension.VariantExt;
import net.minecraft.client.renderer.block.model.Variant;

@Mixin(Variant.class)
public class VariantMixin implements VariantExt {
	@Shadow @Final @Mutable private Transformation rotation;

	@Override
	public Transformation pc$transformation() {
		return rotation;
	}

	@Override
	public void pc$transformation(Transformation transformation) {
		this.rotation = transformation;
	}
}
