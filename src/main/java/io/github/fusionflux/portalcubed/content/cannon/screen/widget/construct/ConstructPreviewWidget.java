package io.github.fusionflux.portalcubed.content.cannon.screen.widget.construct;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.content.cannon.screen.CannonSettingsHolder;
import io.github.fusionflux.portalcubed.content.cannon.screen.ConstructionCannonScreen;
import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.gui.widget.Tickable;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

public class ConstructPreviewWidget extends ConstructWidget implements Tickable {
	public static final Component MESSAGE = ConstructionCannonScreen.translate("construct_preview");

	private final CannonSettingsHolder settings;

	private int ticks = 0;

	public ConstructPreviewWidget(int size, CannonSettingsHolder settings) {
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
	protected void applyConstructTransformations(PoseStack matrices, float delta) {
		float fullTicks = this.ticks + delta;
		float rotation = fullTicks * 2;
		matrices.mulPose(Axis.YP.rotationDegrees(rotation));
	}

	@Override
	public void tick() {
		this.ticks++;
	}
}
