package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.Tooltip;

@Mixin(Tooltip.class)
public interface TooltipAccessor {
	@Accessor
	void setWasHoveredOrFocused(boolean wasHoveredOrFocused);
}
