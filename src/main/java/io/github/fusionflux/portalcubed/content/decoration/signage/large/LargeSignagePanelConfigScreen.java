package io.github.fusionflux.portalcubed.content.decoration.signage.large;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.gui.widget.TitleWidget;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;

public class LargeSignagePanelConfigScreen extends Screen {
	private static final ResourceLocation BACKGROUND = PortalCubed.id("textures/gui/container/signage_panels/large_signage_panel.png");
	public static final int BACKGROUND_WIDTH = 176;
	public static final int BACKGROUND_HEIGHT = 136;

	public static final Component TITLE = Component.translatable("container.portalcubed.large_signage_panel");

	private final LargeSignagePanelBlockEntity largeSignagePanel;

	private int leftPos;
	private int topPos;

    public LargeSignagePanelConfigScreen(LargeSignagePanelBlockEntity largeSignagePanel) {
		super(TITLE);
		this.largeSignagePanel = largeSignagePanel;
	}

	@Override
	protected void init() {
		this.leftPos = (this.width - BACKGROUND_WIDTH) / 2;
        this.topPos = (this.height - BACKGROUND_HEIGHT) / 2;

		LinearLayout root = LinearLayout.horizontal();
		root.defaultCellSetting();
		root.addChild(new TitleWidget(title, font), layoutSettings -> layoutSettings.padding(8, 6));

		// arrange elements
		root.arrangeElements();
		// position in the top left corner of the background
		root.setPosition(this.leftPos, this.topPos);
		root.visitWidgets(this::addRenderableWidget);
		// reverse order of widgets, so they go from top to bottom
		Collections.reverse(children());
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderBackground(graphics, mouseX, mouseY, delta);
		graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public ComponentPath nextFocusPath(FocusNavigationEvent event) {
		return null;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		// close on E
		if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
			this.onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
