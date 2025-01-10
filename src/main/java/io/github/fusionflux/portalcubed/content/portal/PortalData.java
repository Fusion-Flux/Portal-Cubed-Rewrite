package io.github.fusionflux.portalcubed.content.portal;

import org.joml.Quaternionf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;

/**
 * Serializable data for a portal.
 */
public record PortalData(Vec3 origin, Quaternionf rotation, PortalSettings settings) {
	public static final Codec<PortalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Vec3.CODEC.fieldOf("origin").forGetter(PortalData::origin),
			ExtraCodecs.QUATERNIONF.fieldOf("rotation").forGetter(PortalData::rotation),
			PortalSettings.CODEC.fieldOf("settings").forGetter(PortalData::settings)
	).apply(instance, PortalData::new));

	public static final StreamCodec<ByteBuf, PortalData> STREAM_CODEC = StreamCodec.composite(
			PortalCubedStreamCodecs.VEC3, PortalData::origin,
			ByteBufCodecs.QUATERNIONF, PortalData::rotation,
			PortalSettings.STREAM_CODEC, PortalData::settings,
			PortalData::new
	);

	public PortalData withOrigin(Vec3 origin) {
		return new PortalData(origin, this.rotation, this.settings);
	}

	public PortalData withRotation(Quaternionf rotation) {
		return new PortalData(this.origin, rotation, this.settings);
	}

	public PortalData withSettings(PortalSettings settings) {
		return new PortalData(this.origin, this.rotation, settings);
	}
}
