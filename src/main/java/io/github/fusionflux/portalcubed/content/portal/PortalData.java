package io.github.fusionflux.portalcubed.content.portal;

import org.joml.Quaternionf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import io.github.fusionflux.portalcubed.content.portal.graphics.color.PortalColor;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.NonePortalValidator;
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
		boolean render,
		boolean tracer
) {
	public static final Codec<PortalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.fieldOf("creation_tick").forGetter(PortalData::creationTick),
			PortalType.CODEC.fieldOf("type").forGetter(PortalData::type),
			PortalValidator.CODEC.fieldOf("validator").forGetter(PortalData::validator),
			Vec3.CODEC.fieldOf("origin").forGetter(PortalData::origin),
			ExtraCodecs.QUATERNIONF.fieldOf("rotation").forGetter(PortalData::rotation),
			PortalColor.CODEC.fieldOf("color").forGetter(PortalData::color),
			Codec.BOOL.fieldOf("render").forGetter(PortalData::render),
			Codec.BOOL.fieldOf("tracer").forGetter(PortalData::tracer)
	).apply(instance, PortalData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PortalData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, PortalData::creationTick,
			PortalType.STREAM_CODEC, PortalData::type,
			PortalValidator.STREAM_CODEC, PortalData::validator,
			Vec3.STREAM_CODEC, PortalData::origin,
			ByteBufCodecs.QUATERNIONF, PortalData::rotation,
			PortalColor.STREAM_CODEC, PortalData::color,
			ByteBufCodecs.BOOL, PortalData::render,
			ByteBufCodecs.BOOL, PortalData::tracer,
			PortalData::new
	);

	public boolean isValidated() {
		return this.validator != NonePortalValidator.INSTANCE;
	}

	public PortalData withType(Holder<PortalType> type) {
		return new PortalData(this.creationTick, type, this.validator, this.origin, this.rotation, this.color, this.render, this.tracer);
	}

	public PortalData withValidator(PortalValidator validator) {
		return new PortalData(this.creationTick, this.type, validator, this.origin, this.rotation, this.color, this.render, this.tracer);
	}

	public PortalData withOrigin(Vec3 origin) {
		return new PortalData(this.creationTick, this.type, this.validator, origin, this.rotation, this.color, this.render, this.tracer);
	}

	public PortalData withRotation(Quaternionf rotation) {
		return new PortalData(this.creationTick, this.type, this.validator, this.origin, rotation, this.color, this.render, this.tracer);
	}

	public PortalData withColor(PortalColor color) {
		return new PortalData(this.creationTick, this.type, this.validator, this.origin, this.rotation, color, this.render, this.tracer);
	}

	public PortalData withRender(boolean render) {
		return new PortalData(this.creationTick, this.type, this.validator, this.origin, this.rotation, this.color, render, this.tracer);
	}

	public PortalData withTracer(boolean tracer) {
		return new PortalData(this.creationTick, this.type, this.validator, this.origin, this.rotation, this.color, this.render, tracer);
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
				settings.render(),
				settings.tracer()
		);
	}

	public static Quaternionf normalToRotation(Direction normal, float yRot) {
		// vanilla's Direction#getRotation is very weird.
		// UP is the default, corresponding to (0, 0, 0, 1) / new Quaternionf()
		// SOUTH is based on UP, simply rotated 90 degrees around X.
		// Keep the Right Hand Rule in mind. This rotation places the portal upside down on the south face,
		// assuming the top of the portal started facing south (which it does for the default quaternion).
		// DOWN simply continues this, rotating another 90 degrees around X.
		// The 3 remaining directions are interesting. NORTH, WEST, and EAST all start with SOUTH and add
		// a 180, 90, and -90 degree Z rotation, respectively.
		// The Z rotation is evidently applied first. This manifests in-game as these 3 directions actually being
		// rotated around the Y axis, not the Z axis.
		// So UP is the default, SOUTH is a 90-degree X rotation, and DOWN is 180.
		// NORTH, WEST, and EAST are additional rotations around the Y axis starting from SOUTH.
		//
		// So if this puts the texture upside down, wouldn't that apply to vanilla too?
		// Yes! Except it doesn't matter. This method is used exclusively for Shulkers and the block breaking effect.
		// For Shulkers, they're symmetrical cubes, so they're fine. It actually does matter for crumbling, though.
		// Mojang just works around it by adding the necessary 180-degree rotation there.
		Quaternionf rotation = normal.getRotation();
		if (normal.getAxis().isHorizontal()) {
			// make walls right-side-up
			rotation.rotateY(Mth.DEG_TO_RAD * 180);
		}

		return rotation.rotateY(Mth.DEG_TO_RAD * yRot);
	}

	public static Angle normalToFlatRotation(Direction normal, float yRot) {
		return switch (normal) {
			case UP -> Angle.ofDeg(yRot);
			// down is weird since it's rotated 180 around X, not mirrored
			case DOWN -> Angle.ofDeg(-yRot + 180);
			default -> Angle.R0;
		};
	}
}
