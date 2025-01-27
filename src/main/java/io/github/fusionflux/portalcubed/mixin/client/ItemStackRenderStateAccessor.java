package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.item.ItemStackRenderState;

@Mixin(ItemStackRenderState.class)
public interface ItemStackRenderStateAccessor {
	@Accessor
	ItemStackRenderState.LayerRenderState[] getLayers();

	@Accessor
	int getActiveLayerCount();
}
