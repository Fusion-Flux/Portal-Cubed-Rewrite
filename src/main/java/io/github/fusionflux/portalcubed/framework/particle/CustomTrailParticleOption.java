package io.github.fusionflux.portalcubed.framework.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record CustomTrailParticleOption(ParticleType<CustomTrailParticleOption> type, Vec3 target, int color, int duration) implements ParticleOptions {
	public CustomTrailParticleOption(ParticleType<CustomTrailParticleOption> type, Vec3 from, Vec3 to, int color) {
		this(type, to, color, Mth.ceil(from.distanceTo(to) / 2));
	}

	public static MapCodec<CustomTrailParticleOption> codec(ParticleType<CustomTrailParticleOption> type) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				Vec3.CODEC.fieldOf("target").forGetter(CustomTrailParticleOption::target),
				ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter(CustomTrailParticleOption::color),
				ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(CustomTrailParticleOption::duration)
		).apply(instance, (target, color, duration) -> new CustomTrailParticleOption(type, target, color, duration)));
	}

	public static StreamCodec<ByteBuf, CustomTrailParticleOption> streamCodec(ParticleType<CustomTrailParticleOption> type) {
		return StreamCodec.composite(
				Vec3.STREAM_CODEC, CustomTrailParticleOption::target,
				ByteBufCodecs.INT, CustomTrailParticleOption::color,
				ByteBufCodecs.VAR_INT, CustomTrailParticleOption::duration,
				(target, color, duration) -> new CustomTrailParticleOption(type, target, color, duration)
		);
	}

	@Override
	public ParticleType<?> getType() {
		return this.type;
	}
}
