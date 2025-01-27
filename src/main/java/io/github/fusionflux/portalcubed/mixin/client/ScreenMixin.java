package io.github.fusionflux.portalcubed.mixin.client;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.fusionflux.portalcubed.framework.extension.ScreenExt;
import io.github.fusionflux.portalcubed.framework.gui.widget.ScrollbarWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.TickableWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public abstract class ScreenMixin implements ContainerEventHandler, ScreenExt {
	@Unique
	private List<TickableWidget> tickables;

	@Unique
	private List<ScrollbarWidget> scrollBars;

	@Inject(method = "addWidget", at = @At("TAIL"))
	private <T extends GuiEventListener & NarratableEntry> void addSpecial(T child, CallbackInfoReturnable<T> cir) {
		if (child instanceof TickableWidget tickable) {
			if (this.tickables == null)
				this.tickables = new ArrayList<>();
			this.tickables.add(tickable);
		}

		if (child instanceof ScrollbarWidget scrollBar) {
			if (this.scrollBars == null)
				this.scrollBars = new ArrayList<>();
			this.scrollBars.add(scrollBar);
		}
	}

	@Inject(method = "clearWidgets", at = @At("TAIL"))
	private void clearSpecials(CallbackInfo ci) {
		if (this.tickables != null)
			this.tickables.clear();
		if (this.scrollBars != null)
			this.scrollBars.clear();
	}

	@Nullable
	@Override
	public List<TickableWidget> pc$tickables() {
		return this.tickables;
	}

	@Nullable
	@Override
	public List<ScrollbarWidget> pc$scrollBars() {
		return this.scrollBars;
	}
}
