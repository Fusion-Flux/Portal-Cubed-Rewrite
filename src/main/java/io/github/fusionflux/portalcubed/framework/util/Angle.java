package io.github.fusionflux.portalcubed.framework.util;

import java.util.Collection;
import java.util.List;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

// I yearn for the Valhalla
public record Angle(double rad, double deg) {
	public static final Angle R0 = ofDeg(0);
	public static final Angle R90 = ofDeg(90);
	public static final Angle R180 = ofDeg(180);
	public static final Angle R270 = ofDeg(270);
	public static final Collection<Angle> INCREMENTS = List.of(R0, R90, R180, R270);

	public static final Codec<Angle> CODEC = Codec.DOUBLE.xmap(Angle::ofDeg, Angle::deg);
	public static final StreamCodec<ByteBuf, Angle> STREAM_CODEC = ByteBufCodecs.DOUBLE.map(Angle::ofDeg, Angle::deg);

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

	public Angle scale(double value) {
		return ofRad(this.rad * value);
	}

	public float distanceTo(Angle other) {
		return Math.abs(Mth.degreesDifference(this.degF(), other.degF()));
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Angle that && this.rad == that.rad;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(this.rad);
	}

	public static Angle ofRad(double rad) {
		return new Angle(rad, Math.toDegrees(rad));
	}

	public static Angle ofDeg(double deg) {
		return new Angle(Math.toRadians(deg), deg);
	}
}
