package io.github.fusionflux.portalcubed.content.cannon.screen.widget.construct;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.content.cannon.screen.CannonSettingsHolder;
import io.github.fusionflux.portalcubed.content.cannon.screen.ConstructionCannonScreen;
import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.gui.widget.TickableWidget;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

public class ConstructPreviewWidget extends ConstructWidget implements TickableWidget {
	public static final Component MESSAGE = ConstructionCannonScreen.translate("construct_preview");

	private final CannonSettingsHolder settings;

	private int ticks = 0;
	private float rotationOld = 0;
	private float rotation = 0;
	private float rotationAcceleration = 0;

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
	public void onClick(double mouseX, double mouseY) {
		rotationAcceleration += 50;
	}

	@Override
	protected void applyConstructTransformations(PoseStack matrices, float delta) {
		float fullTicks = this.ticks + delta;
		float spin = fullTicks * 2;

		float vel = rotation - rotationOld;
		rotationOld = rotation;
		rotation += Math.max(0, vel - ((delta * delta) / 2)) + rotationAcceleration * delta * delta;
		rotationAcceleration = 0;

		matrices.mulPose(Axis.YP.rotationDegrees(spin + rotation));
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
		// don't
	}

	@Override
	public void tick() {
		this.ticks++;
	}
}
