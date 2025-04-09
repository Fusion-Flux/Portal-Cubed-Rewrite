package io.github.fusionflux.portalcubed.framework.util;

import net.minecraft.util.Mth;

// I yearn for the Valhalla
public record Angle(double rad, double deg) {
	public static final Angle R0 = ofDeg(0);
	public static final Angle R90 = ofDeg(90);
	public static final Angle R180 = ofDeg(180);
	public static final Angle R270 = ofDeg(270);

	public float radF() {
		return (float) this.rad;
	}

	public float degF() {
		return (float) this.deg;
	}

	public double sin() {
		return Math.sin(this.rad);
	}

	public double cos() {
		return Math.cos(this.rad);
	}

	public float distanceTo(Angle other) {
		return Math.abs(Mth.degreesDifference(this.degF(), other.degF()));
	}

	public static Angle ofRad(double rad) {
		return new Angle(rad, Math.toDegrees(rad));
	}

	public static Angle ofDeg(double deg) {
		return new Angle(Math.toRadians(deg), deg);
	}
}
