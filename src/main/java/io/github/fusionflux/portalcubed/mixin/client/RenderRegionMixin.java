package io.github.fusionflux.portalcubed.mixin.client;

import io.github.fusionflux.portalcubed.content.portal.renderer.PortalRenderer;
import io.github.fusionflux.portalcubed.content.portal.RecursionAttachedResource;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderRegion.class, remap = false)
public class RenderRegionMixin {
	@Unique
	private final RecursionAttachedResource<ChunkRenderList> portalRenderList = RecursionAttachedResource.create(() -> new ChunkRenderList((RenderRegion) (Object) this));

	@Inject(method = "getRenderList", at = @At("HEAD"), cancellable = true)
	private void usePortalRenderList(CallbackInfoReturnable<ChunkRenderList> cir) {
		if (PortalRenderer.isRenderingView())
			cir.setReturnValue(this.portalRenderList.get());
	}
}
