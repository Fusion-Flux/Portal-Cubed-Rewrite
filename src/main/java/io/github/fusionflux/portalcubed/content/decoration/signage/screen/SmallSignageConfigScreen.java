package io.github.fusionflux.portalcubed.content.decoration.signage.screen;

import java.util.Locale;

import org.apache.commons.lang3.function.TriConsumer;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.content.decoration.signage.Signage;
import io.github.fusionflux.portalcubed.content.decoration.signage.small.SmallSignageBlock;
import io.github.fusionflux.portalcubed.content.decoration.signage.small.SmallSignageBlockEntity;
import io.github.fusionflux.portalcubed.framework.gui.widget.TabWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton;
import io.github.fusionflux.portalcubed.framework.gui.widget.TitleWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.ToggleButton;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.ConfigureSignageConfigPacket;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;

public class SmallSignageConfigScreen extends SignageConfigScreen {
	public static final int TAB_WIDTH = 44;
	public static final int Y_OFFSET = TabWidget.HEIGHT - 4; // tabs are supposed to slightly overlap the top
	public static final ResourceLocation BACKGROUND = PortalCubed.id("textures/gui/container/signage/small_signage.png");
	public static final int TOGGLE_SIZE = 13;
	public static final ResourceLocation TOGGLE_SPRITE = PortalCubed.id("signage/small_signage");
	public static final Component TOGGLE_TITLE = Component.translatable("container.portalcubed.small_signage.quadrant_toggle");

	public static final Component TITLE = Component.translatable("container.portalcubed.small_signage");
	public static final Component AGED_TITLE = Component.translatable("container.portalcubed.aged_small_signage");

	private final SmallSignageBlockEntity smallSignage;

	private Tab tab;

    public SmallSignageConfigScreen(SmallSignageBlockEntity smallSignage, SmallSignageBlock.Quadrant quadrant) {
		super(smallSignage, smallSignage.aged ? AGED_TITLE : TITLE);
		this.smallSignage = smallSignage;
		this.tab = Tab.fromQuadrant(quadrant);
		this.slotsEnabled = smallSignage.getBlockState()
				.getValue(SmallSignageBlock.QUADRANT_PROPERTIES.get(quadrant));
	}

	private void switchToTab(Tab tab) {
		if (this.tab != tab) {
			this.tab = tab;
			this.slotsEnabled = this.smallSignage.getBlockState()
					.getValue(SmallSignageBlock.QUADRANT_PROPERTIES.get(tab.quadrant()));
			this.resetScrollBar();
			this.rebuildWidgets();
		}
	}

	@Override
	protected int yOffset() {
		return Y_OFFSET;
	}

	@Override
	protected ResourceLocation background() {
		return BACKGROUND;
	}

	@Override
	protected void addExtraElements(TriConsumer<Integer, Integer, LayoutElement> consumer) {
		LinearLayout tabs = LinearLayout.horizontal();
		for (Tab tab : Tab.VALUES) {
			TabWidget button = new TabWidget(TAB_WIDTH, CommonComponents.EMPTY, tab.textures, () -> this.switchToTab(tab));
			if (tab == this.tab)
				button.select();
			tabs.addChild(button);
		}
		consumer.accept(0, 0, tabs);

		LinearLayout quadrantToggle = LinearLayout.horizontal();
		quadrantToggle.addChild(new ToggleButton(TOGGLE_SIZE, TOGGLE_SIZE, TOGGLE_SPRITE, () -> this.slotsEnabled, v -> {
			this.slotsEnabled = v;
			this.rebuildWidgets();
			PortalCubedPackets.sendToServer(new ConfigureSignageConfigPacket.Small(
					this.smallSignage.getBlockPos(),
					this.tab.quadrant(),
					this.slotsEnabled ? TriState.TRUE : TriState.FALSE,
					null
			));
		}));
		quadrantToggle.addChild(
				new TitleWidget(TOGGLE_TITLE, this.font),
				settings -> settings
						.alignVertically(0.8f)
						.paddingLeft(2)
		);
		consumer.accept(14, 109 + Y_OFFSET, quadrantToggle);
	}

	@Override
	protected ResourceKey<Registry<Signage>> registryKey() {
		return PortalCubedRegistries.SMALL_SIGNAGE;
	}

	@Override
	protected Holder<Signage> selectedImage() {
		return this.smallSignage.getQuadrantImage(this.tab.quadrant());
	}

	@Override
	protected void configure(Holder<Signage> image) {
		PortalCubedPackets.sendToServer(new ConfigureSignageConfigPacket.Small(
				this.smallSignage.getBlockPos(),
				this.tab.quadrant(),
				TriState.DEFAULT,
				image
		));
	}

	@Override
	public void tick() {
		if (this.smallSignage.isRemoved())
			this.onClose();
	}

	public enum Tab {
		TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;

		public static final Tab[] VALUES = values();

		public final String name = this.name().toLowerCase(Locale.ROOT);
		public final TexturedStickyButton.Textures textures = TexturedStickyButton.Textures.noHover(
				PortalCubed.id("signage/tab_" + this.name + "_unselected"),
				PortalCubed.id("signage/tab_" + this.name + "_selected")
		);

		public static Tab fromQuadrant(SmallSignageBlock.Quadrant quadrant) {
			return VALUES[quadrant.ordinal()];
		}

		public SmallSignageBlock.Quadrant quadrant() {
			return SmallSignageBlock.Quadrant.VALUES[this.ordinal()];
		}
	}
}
