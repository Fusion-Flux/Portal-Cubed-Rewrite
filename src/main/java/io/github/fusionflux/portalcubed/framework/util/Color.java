package io.github.fusionflux.portalcubed.framework.util;

public record Color(float r, float g, float b, float a) {
	public static final Color RED = new Color(1, 0, 0, 1);
	public static final Color GREEN = new Color(0.5f, 1, 0.5f, 1);
	public static final Color BLUE = new Color(0, 0, 1, 1);
	public static final Color ORANGE = new Color(1, 0.5f, 0, 1);
	public static final Color PURPLE = new Color(0.5f, 0, 1, 1);
	public static final Color CYAN = new Color(0, 1, 1, 1);
}
