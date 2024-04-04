package io.github.fusionflux.portalcubed.content.cannon.screen;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.cannon.data.CannonSettings;
import io.github.fusionflux.portalcubed.content.cannon.screen.tab.ConstructsTab;
import io.github.fusionflux.portalcubed.content.cannon.screen.tab.MaterialsTab;
import io.github.fusionflux.portalcubed.content.cannon.screen.tab.SettingsTab;
import io.github.fusionflux.portalcubed.content.cannon.screen.widget.FloatingWidget;
import io.github.fusionflux.portalcubed.content.cannon.screen.widget.construct.ConstructPreviewWidget;
import io.github.fusionflux.portalcubed.content.cannon.screen.widget.CannonDisplayWidget;
import io.github.fusionflux.portalcubed.content.cannon.screen.widget.TabWidget;
import io.github.fusionflux.portalcubed.framework.gui.layout.PanelLayout;
import io.github.fusionflux.portalcubed.framework.gui.widget.ScrollbarWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.ConfigureCannonPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Locale;

public class ConstructionCannonScreen extends Screen {
	public static final int WIDTH = 176;
	public static final int HEIGHT = 136;
	public static final int BACKGROUND_Y_OFFSET = TabWidget.HEIGHT - 4; // tabs are supposed to slightly overlap the top
	public static final int TAB_TITLE_Y_OFFSET = BACKGROUND_Y_OFFSET + 5;
	public static final int TAB_TITLE_X_OFFSET = 14;
	public static final int SAVE_BUTTON_WIDTH = 80;
	public static final int SAVE_BUTTON_Y = 137;
	public static final int SCROLLBAR_X_OFFSET = 149;
	public static final int SCROLLBAR_Y_OFFSET = 44;

	public static final Component TITLE = Component.translatable("container.portalcubed.construction_cannon");

	private final InteractionHand sourceHand;
	private final CannonSettingsHolder settings;
	// preview is persistent to maintain tick count between tabs
	private final ConstructPreviewWidget constructPreview;
	private ScrollbarWidget scrollBar;

	private Tab tab;

	public ConstructionCannonScreen(InteractionHand hand, CannonSettings settings) {
		super(TITLE);
		this.sourceHand = hand;
		this.settings = new CannonSettingsHolder(settings);
		this.constructPreview = new ConstructPreviewWidget(80, this.settings);
		this.tab = Tab.MATERIALS;
		this.resetScrollBar();
	}

	@Override
	protected void init() {
		super.init();
		LinearLayout root = LinearLayout.horizontal();
		root.defaultCellSetting().paddingHorizontal(3).alignVerticallyMiddle();

		root.addChild(
				FloatingWidget.create("construct_preview", this.constructPreview),
				root.newCellSettings().alignVertically(0.6f)
		);

		PanelLayout menu = root.addChild(new PanelLayout());

		LinearLayout tabs = LinearLayout.horizontal();
		for (int i = 0; i < Tab.values().length; i++) {
			Tab tab = Tab.values()[i];
			TabWidget button = new TabWidget(tab, () -> this.switchToTab(tab));
			if (tab == this.tab) {
				button.select();
			}
			tabs.addChild(button);
			tabs.addChild(SpacerElement.width(1)); // 1-pixel buffer
		}

		menu.addChild(TAB_TITLE_X_OFFSET, TAB_TITLE_Y_OFFSET, new StringWidget(this.tab.title, this.font))
				.setColor(4210752); // magic number from InventoryScreen
		menu.addChild(0, BACKGROUND_Y_OFFSET, ImageWidget.texture(WIDTH, HEIGHT, this.tab.background, 256, 256));
		// add tabs after so they're on top
		menu.addChild(0, 0, tabs);
		switch (this.tab) {
			case MATERIALS -> MaterialsTab.init(this.settings, menu, this.scrollBar);
			case CONSTRUCTS -> ConstructsTab.init(this.settings, menu);
			case SETTINGS -> SettingsTab.init(this.settings, menu);
		}
		if (this.tab != Tab.SETTINGS)
			menu.addChild(SCROLLBAR_X_OFFSET, SCROLLBAR_Y_OFFSET, this.scrollBar);

		// save button
		menu.addChild(
				(WIDTH / 2) - (SAVE_BUTTON_WIDTH / 2),
				SAVE_BUTTON_Y,
				Button.builder(CommonComponents.GUI_DONE, this::save).size(80, 20).build()
		);

		// cannon view
		root.addChild(
				FloatingWidget.create(
						"cannon",
						new CannonDisplayWidget(80, 80, new ItemStack(PortalCubedItems.CONSTRUCTION_CANNON))
				),
				root.newCellSettings().alignVertically(0.6f)
		);

		// first arrangement, set bounds
		root.arrangeElements();
		// center whole thing on main menu
		int x = this.width / 2;
		x -= menu.getWidth() / 2;
		int dx = menu.getX() - root.getX();
		x -= dx;

		int y = this.height / 2;
		y -= menu.getHeight() / 2;

		root.setPosition(x, y);
		// second arrangement, apply new position
		root.arrangeElements();
		root.visitWidgets(this::addRenderableWidget);
		// reverse order of children so that they iterate highest to lowest, allows clicking layered elements
		Collections.reverse(this.children());
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

	private void resetScrollBar() {
		this.scrollBar = new ScrollbarWidget(this.tab.scroller, () -> {
			boolean wasFocused = this.getFocused() == this.scrollBar;
			this.rebuildWidgets();
			if (wasFocused) this.setFocused(this.scrollBar);
		});
	}

	private void switchToTab(Tab tab) {
		if (this.tab != tab) {
			this.tab = tab;
			this.resetScrollBar();
			this.rebuildWidgets();
		}
	}

	private void save(Button saveButton) {
		ConfigureCannonPacket packet = new ConfigureCannonPacket(this.sourceHand, this.settings.get());
		PortalCubedPackets.sendToServer(packet);
		this.onClose();
	}

	public static MutableComponent translate(String key) {
		return Component.translatable("container.portalcubed.construction_cannon." + key);
	}

	public enum Tab {
		MATERIALS, CONSTRUCTS, SETTINGS;

		public final String name = this.name().toLowerCase(Locale.ROOT);
		public final Component title = translate("tab." + this.name);
		public final ResourceLocation background = PortalCubed.id("textures/gui/container/construction_cannon/" + this.name + "_tab.png");
		public final TexturedStickyButton.Textures textures = TexturedStickyButton.Textures.noHover(
				PortalCubed.id("construction_cannon/tab_" + this.name + "_unselected"),
				PortalCubed.id("construction_cannon/tab_" + this.name + "_selected")
		);
		public final ResourceLocation scroller = PortalCubed.id("construction_cannon/" + this.name + "_tab/" + "scroller");
	}
}
