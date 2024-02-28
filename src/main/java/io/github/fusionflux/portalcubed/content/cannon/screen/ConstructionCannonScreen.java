package io.github.fusionflux.portalcubed.content.cannon.screen;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.cannon.data.CannonSettings;
import io.github.fusionflux.portalcubed.content.cannon.screen.tab.MaterialsTab;
import io.github.fusionflux.portalcubed.content.cannon.screen.widget.ConstructPreviewWidget;
import io.github.fusionflux.portalcubed.content.cannon.screen.widget.TabWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class ConstructionCannonScreen extends Screen {
	public static final int WIDTH = 176;
	public static final int HEIGHT = 120;
	public static final int BACKGROUND_Y_OFFSET = TabWidget.HEIGHT - 4; // tabs are supposed to slightly overlap the top
	public static final int TAB_TITLE_Y_OFFSET = BACKGROUND_Y_OFFSET + 5;
	public static final int TAB_TITLE_X_OFFSET = 14;

	public static final Component TITLE = Component.translatable("container.portalcubed.construction_cannon");
	public static final ResourceLocation BACKGROUND = PortalCubed.id("textures/gui/container/construction_cannon/materials_tab.png");

	private final CannonDataHolder settings;

	private Tab tab;

	public ConstructionCannonScreen(CannonSettings settings) {
		super(TITLE);
		this.settings = new CannonDataHolder(settings);
		this.tab = Tab.MATERIALS;
	}

	@Override
	protected void init() {
		super.init();

		ConstructPreviewWidget preview = this.addRenderableWidget(new ConstructPreviewWidget(
				0, 0, this.width / 2, this.height
		));

		for (int i = 0; i < Tab.values().length; i++) {
			Tab tab = Tab.values()[i];
			int x = this.width / 2; // initial
			x += i * TabWidget.WIDTH; // offset by index
			x += i; // 1 buffer pixel between each
			TabWidget button = new TabWidget(
					x, 0, tab, () -> this.switchToTab(tab)
			);
			if (tab == this.tab) {
				button.select();
			}
			this.addRenderableWidget(button);
		}

		switch (this.tab) {
			case MATERIALS -> MaterialsTab.init(this.width / 2, BACKGROUND_Y_OFFSET, this.settings, this::addRenderableWidget);
			case CONSTRUCTS -> {}
			case SETTINGS -> {}
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		// magic from InventoryScreen
		graphics.drawString(
				this.font, this.tab.title,
				(this.width / 2) + TAB_TITLE_X_OFFSET, TAB_TITLE_Y_OFFSET,
				4210752, false
		);
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderBackground(graphics, mouseX, mouseY, delta);
		graphics.blit(BACKGROUND, this.width / 2, BACKGROUND_Y_OFFSET, 0, 0, 256, 256);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void switchToTab(Tab tab) {
		if (this.tab != tab) {
			this.tab = tab;
			this.rebuildWidgets();
		}
	}

	private void save() {
		this.onClose();
	}

	public static Component translate(String key) {
		return Component.translatable("container.portalcubed.construction_cannon." + key);
	}

	public enum Tab {
		MATERIALS, CONSTRUCTS, SETTINGS;

		public final String name = this.name().toLowerCase(Locale.ROOT);
		public final Component title = translate("tab." + this.name);
		public final TexturedStickyButton.Textures textures = TexturedStickyButton.Textures.noHover(
				PortalCubed.id("construction_cannon/tab_" + this.name + "_unselected"),
				PortalCubed.id("construction_cannon/tab_" + this.name + "_selected")
		);
	}
}
