package io.github.fusionflux.portalcubed.content.button.pedestal.screen.widget;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlockEntity;
import io.github.fusionflux.portalcubed.content.button.pedestal.screen.PedestalButtonConfigScreen;
import io.github.fusionflux.portalcubed.framework.gui.widget.HoldableButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class TimerButtonWidget extends HoldableButtonWidget {
	private static final int CLICKS_PER_SPEED = 5;
	private static final float MAX_CLICK_DELAY = 1f / 2f;
	private static final float MAX_CLICK_SPEED = 5f;

	private final PedestalButtonConfigScreen pedestalButton;
	private final boolean up;
	private int clickCounter;
	private float clickSpeed;
	private float clickDelay;

	public TimerButtonWidget(PedestalButtonConfigScreen pedestalButton, boolean up) {
		super(19, 8, new Sprites(PortalCubed.id("pedestal_button/" + "timer_adjust_" + (up ? "up" : "down"))), button -> {
			var self = (TimerButtonWidget) button;
			if (!self.pressed) {
				self.clickCounter = 0;
				self.clickSpeed = 1;
				self.clickDelay = MAX_CLICK_DELAY;
				self.incrementPressTime();
			}
		}, button -> pedestalButton.dirty = true);
		this.pedestalButton = pedestalButton;
		this.up = up;
		tick();
	}

	private void incrementPressTime() {
		var newPressTime = pedestalButton.pressTime + (up ? 20 : -20);
		var clamped = Mth.clamp(newPressTime, PedestalButtonBlockEntity.MIN_PRESS_TIME, PedestalButtonBlockEntity.MAX_PRESS_TIME);
		pedestalButton.pressTime = clamped;
	}

	public void tick() {
		boolean wasActive = active;
		active = pedestalButton.pressTime != (up ? PedestalButtonBlockEntity.MAX_PRESS_TIME : PedestalButtonBlockEntity.MIN_PRESS_TIME);
		if (wasActive && !active)
			onRelease(0, 0);

		if (pressed) {
			if (!isHovered()) {
				onRelease(0, 0);
				return;
			}
			clickDelay -= .1;
			if (clickDelay <= 0) {
				playDownSound(Minecraft.getInstance().getSoundManager());
				incrementPressTime();
				clickDelay = MAX_CLICK_DELAY / clickSpeed;
				if (++clickCounter == CLICKS_PER_SPEED) {
					clickSpeed = Math.min(clickSpeed + .4f, MAX_CLICK_SPEED);
					clickCounter = 0;
				}
			}
		}
	}
}
