package io.github.fusionflux.portalcubed.mixin.client;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.fusionflux.portalcubed.framework.extension.ScreenExt;
import io.github.fusionflux.portalcubed.framework.gui.widget.ScrollbarWidget;
import net.minecraft.Optionull;
import net.minecraft.client.gui.components.events.ContainerEventHandler;

@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMixin {
	@Inject(method = "mouseScrolled", at = @At("RETURN"), cancellable = true)
	default void handleScrollingGloballyForScrollBars(double mouseX, double mouseY, double amount, double d, CallbackInfoReturnable<Boolean> cir) {
		if (this instanceof ScreenExt ext) {
			MutableBoolean scrolled = new MutableBoolean();
			List<ScrollbarWidget> scrollBars = Optionull.map(ext.pc$scrollBars(), List::copyOf);
			if (scrollBars != null) {
				for (ScrollbarWidget scrollBar : scrollBars) {
					if (scrollBar.mouseScrolled(mouseX, mouseY, amount, d))
						scrolled.setValue(true);
				}
			}
			if (scrolled.getValue())
				cir.setReturnValue(true);
		}
	}
}
