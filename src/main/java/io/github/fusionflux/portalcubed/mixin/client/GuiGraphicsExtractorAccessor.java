package io.github.fusionflux.portalcubed.mixin.client;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;

@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {
	@Invoker
	void callSetTooltipForNextFrameInternal(Font font, List<ClientTooltipComponent> lines,
	                                    int xo, int yo, ClientTooltipPositioner positioner,
	                                    @Nullable Identifier style, boolean replaceExisting);
}
