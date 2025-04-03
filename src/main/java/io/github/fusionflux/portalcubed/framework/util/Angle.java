package io.github.fusionflux.portalcubed.framework.util;

// I yearn for the Valhalla
public record Angle(double rad, double deg) {
	public static final Angle ZERO = ofDeg(0);

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

	public static Angle ofRad(double rad) {
		return new Angle(rad, Math.toDegrees(rad));
	}

	public static Angle ofDeg(double deg) {
		return new Angle(Math.toRadians(deg), deg);
	}
}
