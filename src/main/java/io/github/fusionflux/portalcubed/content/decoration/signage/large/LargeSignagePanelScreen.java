package io.github.fusionflux.portalcubed.content.decoration.signage.large;

import io.github.fusionflux.portalcubed.framework.gui.widget.TitleWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Collections;


public class LargeSignagePanelScreen extends Screen {
	public static final int WIDTH = 176;
	public static final int HEIGHT = 136;
	public static final Component TITLE = Component.translatable("container.portalcubed.large_signage_panel");
	private final LargeSignagePanelBlockEntity largeSignagePanel;

    public LargeSignagePanelScreen(LargeSignagePanelBlockEntity largeSignagePanel) {
		super(TITLE);
		this.largeSignagePanel = largeSignagePanel;
	}

	@Override
	protected void init() {
		super.init();
        int leftPos = (width - WIDTH) / 2;
        int topPos = (height - HEIGHT) / 2;
		LinearLayout root = LinearLayout.horizontal();
		root.defaultCellSetting().paddingHorizontal(3).alignVerticallyMiddle();

		root.addChild(new TitleWidget(title, font), layoutSettings -> layoutSettings.padding(8, 6));
		root.arrangeElements();
		// position in the top left corner of the background
		root.setPosition(leftPos, topPos);
		root.visitWidgets(this::addRenderableWidget);
		// reverse order of widgets, so they go from top to bottom
		Collections.reverse(children());
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

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
