package io.github.fusionflux.portalcubed.mixin.client;

import net.minecraft.client.gui.components.Tooltip;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Tooltip.class)
public interface TooltipAccessor {
	@Accessor
	void setWasHoveredOrFocused(boolean wasHoveredOrFocused);
}
