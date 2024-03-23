package io.github.fusionflux.portalcubed.mixin.client;

import io.github.fusionflux.portalcubed.framework.extension.ScreenExt;
import io.github.fusionflux.portalcubed.framework.gui.widget.Tickable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Screen.class)
public class ScreenMixin implements ScreenExt {
	@Unique
	private final List<Tickable> tickables = new ArrayList<>();

	@Inject(method = "addWidget", at = @At("TAIL"))
	private <T extends GuiEventListener & NarratableEntry> void addTickable(T child, CallbackInfoReturnable<T> cir) {
		if (child instanceof Tickable tickable) {
			this.tickables.add(tickable);
		}
	}

	@Inject(method = "clearWidgets", at = @At("TAIL"))
	private void clearTickables(CallbackInfo ci) {
		this.tickables.clear();
	}

	@Override
	public List<Tickable> pc$tickables() {
		return this.tickables;
	}
}
