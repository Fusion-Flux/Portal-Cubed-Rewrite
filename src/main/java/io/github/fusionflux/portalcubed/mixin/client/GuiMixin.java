package io.github.fusionflux.portalcubed.mixin.client;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.framework.extension.ScreenExt;
import io.github.fusionflux.portalcubed.framework.gui.widget.TickableWidget;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Gui.class)
public class GuiMixin {
	@Shadow
	@Nullable
	private Screen screen;

	@Inject(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/Screen;tick()V",
					shift = At.Shift.AFTER
			)
	)
	private void handleScreenTickables(CallbackInfo ci) {
		// this is done because injecting into screen#tick is unreliable, most don't call super.
		if (this.screen instanceof ScreenExt ext) {
			List<TickableWidget> tickables = ext.pc$tickables();
			if (tickables != null)
				tickables.forEach(TickableWidget::tick);
		}
	}
}
