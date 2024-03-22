package io.github.fusionflux.portalcubed.content.button.pedestal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock.Offset;
import io.github.fusionflux.portalcubed.framework.gui.widget.DynamicSpriteWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.TickableWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.TitleWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.ToggleButton;
import io.github.fusionflux.portalcubed.framework.gui.widget.ValueCounterButton;
import io.github.fusionflux.portalcubed.framework.gui.widget.ValueSelectButton;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.ConfigurePedestalButtonPacket;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PedestalButtonConfigScreen extends Screen {
	private static final int BACKGROUND_WIDTH = 131;
	private static final int BACKGROUND_HEIGHT = 91;
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
	private EnumMap<Offset, ValueSelectButton<Offset>> offsetSelectButtons;
	private List<Pair<Tooltip, Layout>> layoutsWithTooltip;

	private static final List<Offset> KONAMI_CODE = List.of(
		Offset.UP,
		Offset.DOWN,
		Offset.LEFT,
		Offset.RIGHT,
		Offset.LEFT,
		Offset.RIGHT,
		Offset.NONE
	);
	private List<Offset> konamiRecord;

	public PedestalButtonConfigScreen(PedestalButtonBlockEntity pedestalButton) {
		super(Component.translatable("container.portalcubed.pedestal_button"));
		this.pedestalButton = pedestalButton;
		this.pressTime = pedestalButton.getPressTime();
		var state = pedestalButton.getBlockState();
		this.offset = state.getValue(PedestalButtonBlock.OFFSET);
		this.base = state.getValue(PedestalButtonBlock.BASE);

		this.konamiRecord = new ArrayList<>(KONAMI_CODE.size());
	}

	private ValueCounterButton pressTimeCounterButton(boolean up) {
		var sprite = PortalCubed.id("pedestal_button/" + "timer_adjust_" + (up ? "up" : "down"));
		return new ValueCounterButton(
			19, 8, sprite,
			up ? 20 : -20, PedestalButtonBlockEntity.PRESS_TIME_RANGE, () -> pressTime, v -> pressTime = v, () -> dirty = true
		);
	}

	@Override
	protected void init() {
		style = Style.choose(this);
		leftPos = (width - BACKGROUND_WIDTH) / 2;
		topPos = (height - BACKGROUND_HEIGHT) / 2;
		offsetSelectButtons = new EnumMap<>(Offset.class);
		layoutsWithTooltip = new ArrayList<>();

		var root = LinearLayout.vertical();
		root.defaultCellSetting().paddingLeft(13);
		root.addChild(new TitleWidget(title, font), settings -> settings.padding(8, 6));

		{
			root.addChild(SpacerElement.height(4));
			var contents = root.addChild(LinearLayout.horizontal());

			{
				var pressTimeCounter = contents.addChild(LinearLayout.vertical());

				{
					var display = pressTimeCounter.addChild(new GridLayout());
					display.addChild(SpacerElement.height(5), 0, 0);
					layoutsWithTooltip.add(Pair.of(Tooltip.create(Component.translatable("container.portalcubed.pedestal_button.press_length")), display));

					{
						display.addChild(SpacerElement.width(5), 1, 0);
						display.addChild(new DynamicSpriteWidget<Integer>(SEGMENT_WIDTH, SEGMENT_HEIGHT, () -> (int) ((pressTime / 20) / 10), val -> style.pressTimeDisplaySegments.get(val)), 1, 1);
						display.addChild(SpacerElement.width(2), 1, 2);
						display.addChild(new DynamicSpriteWidget<Integer>(SEGMENT_WIDTH, SEGMENT_HEIGHT, () -> (int) ((pressTime / 20) % 10), val -> style.pressTimeDisplaySegments.get(val)), 1, 3);
						display.addChild(SpacerElement.width(5), 1, 4);
					}

					display.addChild(SpacerElement.height(5), 2, 0);
				}

				pressTimeCounter.addChild(SpacerElement.height(2));

				{
					var buttons = pressTimeCounter.addChild(LinearLayout.horizontal());

					buttons.addChild(pressTimeCounterButton(false));
					buttons.addChild(pressTimeCounterButton(true));
				}
			}

			contents.addChild(SpacerElement.width(24));

			{
				var offsetSelectButtonGrid = contents.addChild(new GridLayout());
				offsetSelectButtonGrid.spacing(2);
				layoutsWithTooltip.add(Pair.of(Tooltip.create(Component.translatable("container.portalcubed.pedestal_button.offset")), offsetSelectButtonGrid));

				for (var offset : Offset.values()) {
					var button = new ValueSelectButton<Offset>(
						OFFSET_SELECT_WIDTH, OFFSET_SELECT_HEIGHT, OFFSET_SELECT_BASE,
						offset, () -> this.offset, v -> {
							this.offset = v;
							konami(v);
							this.dirty = true;
						}, offsetSelectButtons::get
					);
					offsetSelectButtonGrid.addChild(button, offset.stepY + 1, offset.stepX + 1);
					offsetSelectButtons.put(offset, button);
				}
			}
		}

		{
			root.addChild(SpacerElement.height(5));
			var footer = root.addChild(LinearLayout.horizontal());

			footer.addChild(new ToggleButton(
				BASE_TOGGLE_WIDTH, BASE_TOGGLE_HEIGHT, BASE_TOGGLE_BASE,
				() -> base, v -> {
					base = v;
					dirty = true;
				}
			));
			footer.addChild(new TitleWidget(Component.translatable("container.portalcubed.pedestal_button.base"), font), settings -> settings.alignVerticallyBottom().paddingLeft(2));
		}

		root.arrangeElements();
		root.setPosition(leftPos, topPos);
		root.visitWidgets(this::addRenderableWidget);
		Collections.reverse(children());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		for (var layoutWithTooltip : layoutsWithTooltip) {
			var layout = layoutWithTooltip.right();
			boolean isHovered = mouseX >= layout.getX() && mouseY >= layout.getY() && mouseX < layout.getX() + layout.getWidth() && mouseY < layout.getY() + layout.getHeight();
			if (isHovered)
				setTooltipForNextRenderPass(layoutWithTooltip.left(), DefaultTooltipPositioner.INSTANCE, true);
		}
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderBackground(graphics, mouseX, mouseY, delta);
		graphics.blit(this.style.background, this.leftPos, this.topPos, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
	}

	private void konami(Offset offset) {
		konamiRecord.add(offset);
		if (konamiRecord.equals(KONAMI_CODE)) {
			minecraft.getSoundManager().play(SimpleSoundInstance.forUI(PortalCubedSounds.SURPRISE, 1));
			konamiRecord.clear();
		} else if (konamiRecord.size() >= KONAMI_CODE.size()) {
			konamiRecord.clear();
		}
	}

	@Override
	public void tick() {
		for (var widget : children()) {
			if (widget instanceof TickableWidget tickable)
				tickable.tick();
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
