package io.github.fusionflux.portalcubed.content.cannon.screen.widget.construct;

import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.content.cannon.screen.CannonDataHolder;
import io.github.fusionflux.portalcubed.content.cannon.screen.ConstructionCannonScreen;
import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.gui.widget.Tickable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

public class ConstructPreviewWidget extends ConstructWidget implements Tickable {
	public static final Component MESSAGE = ConstructionCannonScreen.translate("construct_preview");

	private final CannonDataHolder settings;

	private int ticks = 0;

	public ConstructPreviewWidget(int size, CannonDataHolder settings) {
		super(size, MESSAGE);
		this.settings = settings;
	}

	@Override
	@Nullable
	protected ConfiguredConstruct getConstruct() {
		return this.settings.get().construct()
				.map(ConstructManager.INSTANCE::getConstructSet)
				.map(c -> c.preview)
				.orElse(null);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		float fullTicks = this.ticks + delta;
		float rotation = fullTicks * 2;
		graphics.pose().pushPose();
		graphics.pose().mulPose(Axis.YP.rotationDegrees(1));
		super.renderWidget(graphics, mouseX, mouseY, delta);
		graphics.pose().popPose();
	}

	@Override
	public void tick() {
		this.ticks++;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		builder.add(NarratedElementType.TITLE, MESSAGE);
	}
}
