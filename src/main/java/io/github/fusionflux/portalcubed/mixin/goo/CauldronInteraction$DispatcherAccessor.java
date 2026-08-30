package io.github.fusionflux.portalcubed.mixin.goo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;

@Mixin(CauldronInteraction.Dispatcher.class)
public interface CauldronInteraction$DispatcherAccessor {
	@Invoker
	void callPut(Item item, CauldronInteraction interaction);
}
