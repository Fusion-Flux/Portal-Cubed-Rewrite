package io.github.fusionflux.portalcubed.content.button.pedestal.screen;

import java.util.ArrayList;
import java.util.List;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlockEntity;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock.Offset;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PedestalButtonConfigScreen extends Screen {
	public static final int BACKGROUND_WIDTH = 131;
	public static final int BACKGROUND_HEIGHT = 91;

	public static final Component TITLE = Component.translatable("container.portalcubed.pedestal_button");

	public final PedestalButtonBlockEntity pedestalButton;
	private int pressTime;
	private Offset offset;
	private boolean dirty;

	private Style style;
	private int leftPos;
	private int topPos;

	public PedestalButtonConfigScreen(PedestalButtonBlockEntity pedestalButton) {
		super(TITLE);
		this.pedestalButton = pedestalButton;
		this.pressTime = pedestalButton.getPressTime();
		this.offset = pedestalButton.getBlockState().getValue(PedestalButtonBlock.OFFSET);
	}

	@Override
	protected void init() {
		this.style = Style.choose(this);
		this.leftPos = (width - BACKGROUND_WIDTH) / 2;
		this.topPos = (height - BACKGROUND_HEIGHT) / 2;
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
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
		public final ResourceLocation pressTimeDisplayBackground;
		public final List<ResourceLocation> pressTimeDisplaySegments;

		Style(String background, String pressTimeDisplayName) {
			this.background = PortalCubed.id("textures/gui/container/pedestal_buttons/" + background + ".png");
			this.pressTimeDisplayBackground = PortalCubed.id("textures/gui/sprites/pedestal_button/" + pressTimeDisplayName + "background" + ".png");
			this.pressTimeDisplaySegments = new ArrayList<>();
			for (int i = 0; i < 10; i++) {
				this.pressTimeDisplaySegments.add(PortalCubed.id("textures/gui/sprites/pedestal_button/" + pressTimeDisplayName + "_" + i + ".png"));
			}
		}

		public static Style choose(PedestalButtonConfigScreen screen) {
			return screen.pedestalButton.getBlockState().is(PortalCubedBlocks.PEDESTAL_BUTTON) ? NORMAL : OLD_AP;
		}
	}
}
