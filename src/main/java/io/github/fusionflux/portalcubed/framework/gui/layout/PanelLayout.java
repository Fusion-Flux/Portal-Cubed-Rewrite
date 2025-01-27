package io.github.fusionflux.portalcubed.framework.gui.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;

public class PanelLayout implements Layout {
	private final List<Element> elements = new ArrayList<>();

	private int x;
	private int y;
	private int width;
	private int height;

	public <T extends LayoutElement> T addChild(int x, int y, T element) {
		this.elements.add(new Element(x, y, element));
		return element;
	}

	@Override
	public void arrangeElements() {
		if (this.elements.isEmpty())
			return;

		this.elements.forEach(element -> {
			element.element.setX(this.x + element.x);
			element.element.setY(this.y + element.y);
		});

		Layout.super.arrangeElements();

		this.width = Integer.MIN_VALUE;
		this.height = Integer.MIN_VALUE;
		for (Element elementWrapper : this.elements) {
			LayoutElement element = elementWrapper.element;

			int dx = element.getX() - this.x;
			int width = element.getWidth();
			int totalWidth = dx + width;
			this.width = Math.max(this.width, totalWidth);

			int dy = element.getY() - this.y;
			int height = element.getHeight();
			int totalHeight = dy + height;
			this.height = Math.max(this.height, totalHeight);
		}
	}

	@Override
	public void visitChildren(Consumer<LayoutElement> widgetConsumer) {
		this.elements.forEach(element -> widgetConsumer.accept(element.element));
	}

	@Override
	public void setX(int x) {
		this.x = x;
		this.arrangeElements();
	}

	@Override
	public void setY(int y) {
		this.y = y;
		this.arrangeElements();
	}

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	public record Element(int x, int y, LayoutElement element) {
	}
}
