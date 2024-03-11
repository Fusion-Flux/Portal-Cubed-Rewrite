package io.github.fusionflux.portalcubed.content.button.pedestal.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlockEntity;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock.Offset;
import io.github.fusionflux.portalcubed.content.button.pedestal.screen.widget.TimerButtonWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.DynamicSpriteWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.ToggleButtonWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.ValueSelectionWidget;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.ConfigurePedestalButtonPacket;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PedestalButtonConfigScreen extends Screen {
	private static final int BACKGROUND_WIDTH = 131;
	private static final int BACKGROUND_HEIGHT = 91;
	private static final int TITLE_X_OFFSET = 8;
	private static final int TITLE_Y_OFFSET = 6;
	private static final int CONTENT_X_OFFSET = 13;
	private static final int SEGMENT_WIDTH = 13;
	private static final int SEGMENT_HEIGHT = 23;
	private static final int OFFSET_SELECT_WIDTH = 13;
	private static final int OFFSET_SELECT_HEIGHT = OFFSET_SELECT_WIDTH;
	private static final ResourceLocation OFFSET_SELECT_BASE = PortalCubed.id("pedestal_button/offset_select");
	private static final int BASE_TOGGLE_WIDTH = 11;
	private static final int BASE_TOGGLE_HEIGHT = BASE_TOGGLE_WIDTH;
	private static final ResourceLocation BASE_TOGGLE_BASE = PortalCubed.id("pedestal_button/base_toggle");

	private final PedestalButtonBlockEntity pedestalButton;
	public int pressTime;
	private Offset offset;
	private boolean base;
	public boolean dirty;

	private Style style;
	private int leftPos;
	private int topPos;
	private EnumMap<Offset, ValueSelectionWidget<Offset>> offsetSelectButtons;

	public PedestalButtonConfigScreen(PedestalButtonBlockEntity pedestalButton) {
		super(Component.translatable("container.portalcubed.pedestal_button"));
		this.pedestalButton = pedestalButton;
		this.pressTime = pedestalButton.getPressTime();
		var state = pedestalButton.getBlockState();
		this.offset = state.getValue(PedestalButtonBlock.OFFSET);
		this.base = state.getValue(PedestalButtonBlock.BASE);
	}

	@Override
	protected void init() {
		style = Style.choose(this);
		leftPos = (width - BACKGROUND_WIDTH) / 2;
		topPos = (height - BACKGROUND_HEIGHT) / 2;
		offsetSelectButtons = new EnumMap<>(Offset.class);

		var root = LinearLayout.vertical();
		root.defaultCellSetting().paddingLeft(CONTENT_X_OFFSET);
		root.spacing(5);

		{
			var contents = root.addChild(LinearLayout.horizontal());
			contents.defaultCellSetting().paddingTop(25);
			contents.spacing(24);

			{
				var pressTimeDisplay = contents.addChild(LinearLayout.vertical());
				pressTimeDisplay.spacing(2);

				{
					var pressTimeDisplaySegments = pressTimeDisplay.addChild(LinearLayout.horizontal());
					var cellSettings = pressTimeDisplaySegments.defaultCellSetting().paddingTop(5).paddingBottom(5);
					pressTimeDisplaySegments.spacing(2);

					cellSettings.paddingLeft(5);
					pressTimeDisplaySegments.addChild(new DynamicSpriteWidget<Integer>(SEGMENT_WIDTH, SEGMENT_HEIGHT, () -> (int) ((pressTime / 20) / 10), val -> style.pressTimeDisplaySegments.get(val)));
					cellSettings.paddingLeft(0);
					pressTimeDisplaySegments.addChild(new DynamicSpriteWidget<Integer>(SEGMENT_WIDTH, SEGMENT_HEIGHT, () -> (int) ((pressTime / 20) % 10), val -> style.pressTimeDisplaySegments.get(val)));
				}

				{
					var pressTimeDisplayButtons = pressTimeDisplay.addChild(LinearLayout.horizontal());

					pressTimeDisplayButtons.addChild(new TimerButtonWidget(this, false));
					pressTimeDisplayButtons.addChild(new TimerButtonWidget(this, true));
				}
			}

			{
				var offsetSelectButtonGrid = contents.addChild(new GridLayout());
				offsetSelectButtonGrid.spacing(2);

				for (var offset : Offset.values()) {
					var button = new ValueSelectionWidget<Offset>(
						OFFSET_SELECT_WIDTH, OFFSET_SELECT_HEIGHT, OFFSET_SELECT_BASE,
						offset, () -> this.offset, v -> {
							this.offset = v;
							this.dirty = true;
						}, offsetSelectButtons::get
					);
					offsetSelectButtonGrid.addChild(button, offset.stepY + 1, offset.stepX + 1);
					offsetSelectButtons.put(offset, button);
				}
			}
		}

		{
			root.addChild(new ToggleButtonWidget(
				BASE_TOGGLE_WIDTH, BASE_TOGGLE_HEIGHT, BASE_TOGGLE_BASE,
				() -> base, v -> {
					base = v;
					dirty = true;
				}
			));
		}

		root.arrangeElements();
		root.setPosition(leftPos, topPos);
		root.visitWidgets(this::addRenderableWidget);
		Collections.reverse(children());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		graphics.drawString(font, title, leftPos + TITLE_X_OFFSET, topPos + TITLE_Y_OFFSET, 4210752, false);
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderBackground(graphics, mouseX, mouseY, delta);
		graphics.blit(this.style.background, this.leftPos, this.topPos, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
	}

	@Override
	public void tick() {
		for (var widget : children()) {
			if (widget instanceof TimerButtonWidget timerButton)
				timerButton.tick();
		}

		if (dirty) {
			PortalCubedPackets.sendToServer(new ConfigurePedestalButtonPacket(pedestalButton.getBlockPos(), pressTime, offset, base));
			dirty = false;
		}
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
	public void onClose() {
		super.onClose();
		tick();
	}

	public static enum Style {
		NORMAL("pedestal_button", "7_segment"),
		OLD_AP("old_ap_pedestal_button", "neon");

		public final ResourceLocation background;
		public final List<ResourceLocation> pressTimeDisplaySegments;

		Style(String background, String pressTimeDisplayName) {
			this.background = PortalCubed.id("textures/gui/container/pedestal_buttons/" + background + ".png");
			this.pressTimeDisplaySegments = new ArrayList<>();
			for (int i = 0; i < 10; i++) {
				this.pressTimeDisplaySegments.add(PortalCubed.id("pedestal_button/" + pressTimeDisplayName + "_" + i));
			}
		}

		public static Style choose(PedestalButtonConfigScreen screen) {
			return screen.pedestalButton.getBlockState().is(PortalCubedBlocks.PEDESTAL_BUTTON) ? NORMAL : OLD_AP;
		}
	}
}
