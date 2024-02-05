package io.github.fusionflux.portalcubed.content.cannon.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ConstructionCannonScreen extends AbstractContainerScreen<ConstructionCannonMenu> {
	public ConstructionCannonScreen(ConstructionCannonMenu handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
	}

	@Override
	protected void init() {
		super.init();


	}

	@Override
	protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
	}
}
