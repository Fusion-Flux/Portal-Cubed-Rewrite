package io.github.fusionflux.portalcubed.content.button.pedestal.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlockEntity;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock.Offset;
import io.github.fusionflux.portalcubed.framework.gui.widget.SimpleButtonWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.SimpleButtonWidget.Sprites;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class PedestalButtonConfigScreen extends Screen {
	private static final int BACKGROUND_WIDTH = 131;
	private static final int BACKGROUND_HEIGHT = 91;
	private static final int TITLE_X_OFFSET = 8;
	private static final int TITLE_Y_OFFSET = 6;
	private static final int CONTENT_X_OFFSET = 13;
	private static final int SEGMENT_WIDTH = 13;
	private static final int SEGMENT_HEIGHT = 23;

	private final PedestalButtonBlockEntity pedestalButton;
	private int pressTime;
	private Offset offset;
	private boolean dirty;

	private Style style;
	private int leftPos;
	private int topPos;

	public PedestalButtonConfigScreen(PedestalButtonBlockEntity pedestalButton) {
		super(Component.translatable("container.portalcubed.pedestal_button"));
		this.pedestalButton = pedestalButton;
		this.pressTime = pedestalButton.getPressTime();
		this.offset = pedestalButton.getBlockState().getValue(PedestalButtonBlock.OFFSET);
	}

	private SimpleButtonWidget createTimerAdjustButton(boolean up) {
		var spriteId = PortalCubed.id("pedestal_button/" + "timer_adjust_" + (up ? "up" : "down"));
		return new SimpleButtonWidget(19, 8, new Sprites(spriteId, spriteId.withSuffix("_hover")), () -> {
			pressTime = Mth.clamp(pressTime + (up ? 20 : -20), 20, 99 * 20);
			dirty = true;
		});
	}

	@Override
	protected void init() {
		style = Style.choose(this);
		leftPos = (width - BACKGROUND_WIDTH) / 2;
		topPos = (height - BACKGROUND_HEIGHT) / 2;

		var root = LinearLayout.vertical();
		root.defaultCellSetting().paddingLeft(13);
		root.spacing(5);

		{
			var contents = root.addChild(LinearLayout.horizontal());
			contents.defaultCellSetting();

			{
				var pressTimeDisplay = contents.addChild(LinearLayout.vertical());

				{
					var pressTimeDisplaySegments = pressTimeDisplay.addChild(LinearLayout.horizontal());
					var cellSettings = pressTimeDisplaySegments.defaultCellSetting().paddingTop(30).paddingBottom(5);
					pressTimeDisplaySegments.spacing(2);

					cellSettings.paddingLeft(5);
					pressTimeDisplaySegments.addChild(new ValueSpriteWidget(SEGMENT_WIDTH, SEGMENT_HEIGHT, () -> (int) ((pressTime / 20) / 10), val -> style.pressTimeDisplaySegments.get(val)));
					cellSettings.paddingLeft(0);
					pressTimeDisplaySegments.addChild(new ValueSpriteWidget(SEGMENT_WIDTH, SEGMENT_HEIGHT, () -> (int) ((pressTime / 20) % 10), val -> style.pressTimeDisplaySegments.get(val)));
				}

				{
					var pressTimeDisplayButtons = pressTimeDisplay.addChild(LinearLayout.horizontal());
					pressTimeDisplayButtons.defaultCellSetting().paddingTop(2);

					pressTimeDisplayButtons.addChild(createTimerAdjustButton(true));
					pressTimeDisplayButtons.addChild(createTimerAdjustButton(false));
				}
			}
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
		if (dirty) {
			dirty = false;
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
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
