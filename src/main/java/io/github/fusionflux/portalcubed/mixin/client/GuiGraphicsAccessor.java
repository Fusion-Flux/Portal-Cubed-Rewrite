package io.github.fusionflux.portalcubed.mixin.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.ResourceLocation;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccessor {
	@Invoker
	void callRenderTooltipInternal(Font textRenderer, List<ClientTooltipComponent> tooltip, int x, int y, ClientTooltipPositioner positioner, @Nullable ResourceLocation sprite);
}
