package io.github.fusionflux.portalcubed.content.portal;

import org.joml.Quaternionf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.color.PortalColor;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.PortalValidator;
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
		PortalValidator validator,
		Vec3 origin,
		Quaternionf rotation,
		PortalColor color,
		boolean render
) {
	public static final Codec<PortalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.fieldOf("creation_tick").forGetter(PortalData::creationTick),
			PortalType.CODEC.fieldOf("type").forGetter(PortalData::type),
			PortalValidator.CODEC.fieldOf("validator").forGetter(PortalData::validator),
			Vec3.CODEC.fieldOf("origin").forGetter(PortalData::origin),
			ExtraCodecs.QUATERNIONF.fieldOf("rotation").forGetter(PortalData::rotation),
			PortalColor.CODEC.fieldOf("color").forGetter(PortalData::color),
			Codec.BOOL.fieldOf("render").forGetter(PortalData::render)
	).apply(instance, PortalData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PortalData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, PortalData::creationTick,
			PortalType.STREAM_CODEC, PortalData::type,
			PortalValidator.STREAM_CODEC, PortalData::validator,
			Vec3.STREAM_CODEC, PortalData::origin,
			ByteBufCodecs.QUATERNIONF, PortalData::rotation,
			PortalColor.STREAM_CODEC, PortalData::color,
			ByteBufCodecs.BOOL, PortalData::render,
			PortalData::new
	);

	public PortalData withType(Holder<PortalType> type) {
		return new PortalData(this.creationTick, type, this.validator, this.origin, this.rotation, this.color, this.render);
	}

	public PortalData withValidator(PortalValidator validator) {
		return new PortalData(this.creationTick, this.type, validator, this.origin, this.rotation, this.color, this.render);
	}

	public PortalData withOrigin(Vec3 origin) {
		return new PortalData(this.creationTick, this.type, this.validator, origin, this.rotation, this.color, this.render);
	}

	public PortalData withRotation(Quaternionf rotation) {
		return new PortalData(this.creationTick, this.type, this.validator, this.origin, rotation, this.color, this.render);
	}

	public PortalData withColor(PortalColor color) {
		return new PortalData(this.creationTick, this.type, this.validator, this.origin, this.rotation, color, this.render);
	}

	public PortalData withRender(boolean render) {
		return new PortalData(this.creationTick, this.type, this.validator, this.origin, this.rotation, this.color, render);
	}

	public static PortalData createWithSettings(Level world, Vec3 origin, Quaternionf rotation, PortalValidator validator, PortalSettings settings) {
		Holder<PortalType> type = world.registryAccess()
				.get(settings.typeId())
				.orElseThrow(() -> new IllegalStateException("Missing portal type " + settings.typeId()));
		return new PortalData(
				world.getGameTime(),
				type,
				validator,
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
		// we don't want to 180 the yRot for flat surfaces
		return Angle.ofDeg(normal.getAxis().isHorizontal() ? 0 : degreesForNormal(normal, yRot));
	}

	private static float degreesForNormal(Direction normal, float yRot) {
		// vanilla treats rotations like they're on the outside of a box, not the inside.
		// the easiest way to handle this is to just 180 the yRot on horizontal axes.
		return switch (normal) {
			case UP -> yRot;
			case DOWN -> -yRot;
			default -> 180;
		};
	}
}
