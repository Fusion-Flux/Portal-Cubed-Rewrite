package io.github.fusionflux.portalcubed.content.portal;

import org.joml.Quaternionf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.util.Angle;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Serializable data for a portal.
 */
public record PortalData(
		long creationTick,
		Holder<PortalType> type,
		boolean validate,
		Vec3 origin,
		Quaternionf rotation,
		int color,
		boolean render
) {
	public static final Codec<PortalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.fieldOf("creation_tick").forGetter(PortalData::creationTick),
			PortalType.CODEC.fieldOf("type").forGetter(PortalData::type),
			Codec.BOOL.fieldOf("validate").forGetter(PortalData::validate),
			Vec3.CODEC.fieldOf("origin").forGetter(PortalData::origin),
			ExtraCodecs.QUATERNIONF.fieldOf("rotation").forGetter(PortalData::rotation),
			ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter(PortalData::color),
			Codec.BOOL.fieldOf("render").forGetter(PortalData::render)
	).apply(instance, PortalData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PortalData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, PortalData::creationTick,
			PortalType.STREAM_CODEC, PortalData::type,
			ByteBufCodecs.BOOL, PortalData::validate,
			Vec3.STREAM_CODEC, PortalData::origin,
			ByteBufCodecs.QUATERNIONF, PortalData::rotation,
			ByteBufCodecs.INT, PortalData::color,
			ByteBufCodecs.BOOL, PortalData::render,
			PortalData::new
	);

	public PortalData withType(Holder<PortalType> type) {
		return new PortalData(this.creationTick, type, this.validate, this.origin, this.rotation, this.color, this.render);
	}

	public PortalData withValidate(boolean validate) {
		return new PortalData(this.creationTick, this.type, validate, this.origin, this.rotation, this.color, this.render);
	}

	public PortalData withOrigin(Vec3 origin) {
		return new PortalData(this.creationTick, this.type, this.validate, origin, this.rotation, this.color, this.render);
	}

	public PortalData withRotation(Quaternionf rotation) {
		return new PortalData(this.creationTick, this.type, this.validate, this.origin, rotation, this.color, this.render);
	}

	public PortalData withColor(int color) {
		return new PortalData(this.creationTick, this.type, this.validate, this.origin, this.rotation, color, this.render);
	}

	public PortalData withRender(boolean render) {
		return new PortalData(this.creationTick, this.type, this.validate, this.origin, this.rotation, this.color, render);
	}

	public static PortalData createWithSettings(Level world, Vec3 origin, Quaternionf rotation, PortalSettings settings) {
		Holder<PortalType> type = world.registryAccess()
				.get(settings.typeId())
				.orElseThrow(() -> new IllegalStateException("Missing portal type " + settings.typeId()));
		return new PortalData(
				world.getGameTime(),
				type,
				settings.validate(),
				origin,
				rotation,
				settings.color(),
				settings.render()
		);
	}

	public static Quaternionf normalToRotation(Direction normal, float yRot) {
		return normal.getRotation().rotateY(Mth.DEG_TO_RAD * degreesForNormal(normal, yRot));
	}

	public static Angle normalToFlatRotation(Direction normal, float yRot) {
		return Angle.ofDeg(degreesForNormal(normal, yRot));
	}

	private static float degreesForNormal(Direction normal, float yRot) {
		// vanilla treats rotations like they're on the outside of a box, not the inside.
		// the easiest way to handle this is to just 180 the yRot on horizontal axes.
		return switch (normal) {
			case UP -> -yRot;
			case DOWN -> yRot;
			default -> 180;
		};
	}
}
