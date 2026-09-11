package io.github.fusionflux.portalcubed.framework.util;

import net.minecraft.util.ARGB;

/// Common colors used in debug rendering. Each value is ARGB packed into an int.
/// @see ARGB
public final class Colors {
	public static final int RED = rgb(1, 0, 0);
	public static final int GREEN = rgb(0.5f, 1, 0.5f);
	public static final int BLUE = rgb(0, 0, 1);
	public static final int ORANGE = rgb(1, 0.5f, 0);
	public static final int PURPLE = rgb(0.5f, 0, 1);
	public static final int CYAN = rgb(0, 1, 1);
	public static final int YELLOW = rgb(1, 0.95f, 0);

	private Colors() {}

	private static int rgb(float r, float g, float b) {
		return ARGB.colorFromFloat(1, r, g, b);
	}
}
