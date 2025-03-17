package io.github.fusionflux.portalcubed.content.cannon.screen.widget.construct;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import io.github.fusionflux.portalcubed.content.cannon.screen.CannonSettingsHolder;
import io.github.fusionflux.portalcubed.content.cannon.screen.ConstructionCannonScreen;
import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.gui.widget.TickableWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class ConstructPreviewWidget extends ConstructWidget implements TickableWidget {
	public static final Component MESSAGE = ConstructionCannonScreen.translate("construct_preview");

	private final CannonSettingsHolder settings;

	private int ticks;
	private float rotationOld;
	private float rotation;
	private float rotationAcceleration;

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
		this.rotationAcceleration += 50;
	}

	@Override
	protected void applyConstructTransformations(PoseStack matrices, float delta) {
		float partialTick = Minecraft.getInstance().getDeltaTracker()
				.getGameTimeDeltaPartialTick(false);
		float fullTicks = this.ticks + partialTick;
		float spin = fullTicks * 2;

		float vel = this.rotation - this.rotationOld;
		this.rotationOld = this.rotation;
		this.rotation += Math.max(0, vel - ((delta * delta) / 2)) + this.rotationAcceleration * delta * delta;
		this.rotationAcceleration = 0;

		matrices.mulPose(Axis.YP.rotationDegrees(spin + this.rotation));
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
