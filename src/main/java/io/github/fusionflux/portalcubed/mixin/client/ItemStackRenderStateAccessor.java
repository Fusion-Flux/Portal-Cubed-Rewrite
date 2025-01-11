package io.github.fusionflux.portalcubed.mixin.client;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemStackRenderState.class)
public interface ItemStackRenderStateAccessor {
	@Invoker
	ItemStackRenderState.LayerRenderState callFirstLayer();
}
