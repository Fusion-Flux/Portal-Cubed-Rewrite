package io.github.fusionflux.portalcubed.mixin.client;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.fusionflux.portalcubed.framework.extension.ScreenExt;
import net.minecraft.client.gui.components.events.ContainerEventHandler;

@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMixin {
	// because mojank, mouseScrolled only runs on widgets if you are hovering over them
	@Inject(method = "mouseScrolled", at = @At("RETURN"), cancellable = true)
	default void whyMoJank(double mouseX, double mouseY, double amount, double d, CallbackInfoReturnable<Boolean> cir) {
		if (this instanceof ScreenExt ext) {
			var scrolled = new AtomicBoolean();
			var scrollBars = ext.pc$scrollBars();
			if (scrollBars != null) {
				// Copy because scrollbars rebuild all widgets
				new ArrayList<>(scrollBars)
					.forEach(scrollBar -> scrolled.compareAndSet(false, scrollBar.mouseScrolled(mouseX, mouseY, amount, d)));
			}
			if (scrolled.get())
				cir.setReturnValue(true);
		}
	}
}
