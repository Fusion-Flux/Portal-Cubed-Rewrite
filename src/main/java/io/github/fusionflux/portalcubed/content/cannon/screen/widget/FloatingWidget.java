package io.github.fusionflux.portalcubed.content.cannon.screen.widget;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.gui.layout.PanelLayout;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.resources.ResourceLocation;

public class FloatingWidget {
	public static LayoutElement create(String name, LayoutElement element) {
		ResourceLocation border = PortalCubed.id("construction_cannon/" + name + "/border");
		ResourceLocation background = PortalCubed.id("construction_cannon/" + name + "/background");

		PanelLayout layout = new PanelLayout();

		// background first for right render order
		layout.addChild(7, 7, ImageWidget.sprite(80, 80, background));
		layout.addChild(0, 0, ImageWidget.sprite(94, 94, border));
		layout.addChild(7, 7, element);

		return layout;
	}
}
