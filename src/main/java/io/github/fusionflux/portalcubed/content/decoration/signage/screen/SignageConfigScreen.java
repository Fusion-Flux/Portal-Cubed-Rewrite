package io.github.fusionflux.portalcubed.content.decoration.signage.screen;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;

import org.apache.commons.lang3.function.TriConsumer;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.decoration.signage.SignageBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.screen.widget.SignageSlotWidget;
import io.github.fusionflux.portalcubed.framework.gui.layout.PanelLayout;
import io.github.fusionflux.portalcubed.framework.gui.widget.ScrollbarWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton;
import io.github.fusionflux.portalcubed.framework.gui.widget.TitleWidget;
import io.github.fusionflux.portalcubed.framework.signage.Signage;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public abstract class SignageConfigScreen extends Screen {
	public static final int WIDTH = 176;
	public static final int HEIGHT = 136;
	public static final ResourceLocation SCROLLER = PortalCubed.id("signage/scroller");
	public static final int SLOT_ROWS = 4;
	public static final int SLOT_COLUMNS = 6;
	public static final int SLOT_GRID_SIZE = SLOT_COLUMNS * SLOT_ROWS;

	private final RegistryAccess registryAccess;
	private final SignageBlockEntity signageBlock;

	protected boolean slotsEnabled = true;

	private int leftPos;
	private int topPos;
	private ScrollbarWidget scrollBar;

	SignageConfigScreen(SignageBlockEntity signageBlock, Component title) {
		super(title);
		this.registryAccess = Objects.requireNonNull(signageBlock.getLevel()).registryAccess();
		this.signageBlock = signageBlock;
		this.resetScrollBar();
	}

	protected abstract ResourceLocation background();

	protected abstract void addExtraElements(TriConsumer<Integer, Integer, LayoutElement> consumer);

	protected abstract ResourceKey<Registry<Signage>> registryKey();

	protected abstract Holder<Signage> selectedSignage();

	protected abstract void updateSignage(Holder<Signage> holder);

	protected int yOffset() {
		return 0;
	}

	protected final void resetScrollBar() {
		this.scrollBar = new ScrollbarWidget(SCROLLER, () -> {
			boolean wasFocused = this.getFocused() == this.scrollBar;
			this.rebuildWidgets();
			if (wasFocused) this.setFocused(this.scrollBar);
		});
	}

	@Override
	protected final void init() {
		super.init();
		PanelLayout root = new PanelLayout();
		this.addExtraElements(root::addChild);
		root.addChild(8, 6 + this.yOffset(), new TitleWidget(this.title, this.font));

		GridLayout slots = root.addChild(15, 16 + this.yOffset(), new GridLayout());
		List<Holder.Reference<Signage>> signage = this.registryAccess
				.lookupOrThrow(this.registryKey())
				.listElements()
				.sorted(Comparator.comparing(holder -> holder.key().location()))
				.toList();

		int rowCount = Mth.positiveCeilDiv(signage.size(), SLOT_COLUMNS) - SLOT_ROWS;
		int scrollRowPos = Math.max((int) ((this.scrollBar.scrollPos() * rowCount) + .5f), 0);
		int i = -(SLOT_COLUMNS * scrollRowPos);
		this.scrollBar.active = false;
		if (rowCount > 0) {
			this.scrollBar.active = true;
			this.scrollBar.scrollRate = 1f / rowCount;
		}

		for (Holder.Reference<Signage> holder : signage) {
			if (i >= 0) {
				SignageSlotWidget slot = new SignageSlotWidget(holder.value(), holder.key().isFor(PortalCubedRegistries.SMALL_SIGNAGE), this.signageBlock.aged, () -> {
					slots.visitWidgets(widget -> ((TexturedStickyButton) widget).deselect());
					this.updateSignage(holder);
				});
				slot.active = this.slotsEnabled;
				if (this.selectedSignage() == holder)
					slot.select();
				slots.addChild(slot, i / SLOT_COLUMNS, i % SLOT_COLUMNS);
			}
			++i;
			if (i >= SLOT_GRID_SIZE) break;
		}
		root.addChild(149, 16 + this.yOffset(), this.scrollBar);

		// arrange elements
		root.arrangeElements();
		// position in the top left corner of the background
		this.leftPos = (this.width - WIDTH) / 2;
		this.topPos = (this.height - HEIGHT) / 2;
		root.setPosition(this.leftPos, this.topPos);
		root.visitWidgets(this::addRenderableWidget);
		// reverse order of widgets, so they go from top to bottom
		Collections.reverse(this.children());
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		this.renderTransparentBackground(graphics);
		graphics.blit(RenderType::guiTextured, this.background(), this.leftPos, this.topPos + this.yOffset(), 0, 0, WIDTH, HEIGHT, 256, 256);
	}

	@Override
	public void tick() {
		if (this.signageBlock.isRemoved())
			this.onClose();
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
