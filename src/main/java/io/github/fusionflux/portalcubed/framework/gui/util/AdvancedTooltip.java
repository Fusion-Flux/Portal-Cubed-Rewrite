package io.github.fusionflux.portalcubed.framework.gui.util;

import java.util.ArrayList;
import java.util.List;

import io.github.fusionflux.portalcubed.mixin.client.GuiGraphicsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class AdvancedTooltip {
	private final Factory factory;
	private List<ClientTooltipComponent> components;
	private boolean lastAdvancedTooltips;

	public AdvancedTooltip(Factory factory) {
		this.factory = factory;
	}

	public List<ClientTooltipComponent> get() {
		boolean advancedTooltips = Minecraft.getInstance().options.advancedItemTooltips;
		if (this.lastAdvancedTooltips != advancedTooltips) {
			this.components = null;
			this.lastAdvancedTooltips = advancedTooltips;
		}

		if (this.components == null) {
			Builder builder = new Builder(advancedTooltips);
			this.factory.build(builder);
			this.components = builder.components;
		}

		return this.components;
	}

	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		GuiGraphicsAccessor access = (GuiGraphicsAccessor) graphics;
		Font font = Minecraft.getInstance().font;
		access.callRenderTooltipInternal(font, this.get(), mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
	}

	public interface Factory {
		void build(Builder builder);
	}

	public static final class Builder {
		private final List<ClientTooltipComponent> components = new ArrayList<>();
		public final boolean advanced;

		public Builder(boolean advanced) {
			this.advanced = advanced;
		}

		public void add(Component component) {
			this.add(ClientTooltipComponent.create(component.getVisualOrderText()));
		}

		public void add(TooltipComponent component) {
			this.add(ClientTooltipComponent.create(component));
		}

		public void add(ClientTooltipComponent component) {
			this.components.add(component);
		}
	}
}
