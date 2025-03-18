package io.github.fusionflux.portalcubed.mixin.portals.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.renderer.PortalRenderer;
import io.github.fusionflux.portalcubed.content.portal.renderer.RecursionAttachedResource;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;

@Mixin(value = RenderRegion.class, remap = false)
public class RenderRegionMixin {
	@Unique
	private final RecursionAttachedResource<ChunkRenderList> portalRenderList = RecursionAttachedResource.create(() -> new ChunkRenderList((RenderRegion) (Object) this));

	@ModifyReturnValue(method = "getRenderList", at = @At("RETURN"))
	private ChunkRenderList usePortalRenderList(ChunkRenderList original) {
		return PortalRenderer.isRenderingView() ? this.portalRenderList.get() : original;
	}
}
