package io.github.fusionflux.portalcubed.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.fusionflux.portalcubed.framework.extension.BakedQuadExt;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.minecraft.client.renderer.block.model.BakedQuad;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements BakedQuadExt {
	@Unique
	private RenderMaterial material;

	@Override
	@Nullable
	public RenderMaterial pc$renderMaterial() {
		return this.material;
	}

	@Override
	public void pc$setRenderMaterial(RenderMaterial material) {
		this.material = material;
	}
}
