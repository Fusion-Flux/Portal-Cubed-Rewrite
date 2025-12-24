package io.github.fusionflux.portalcubed.content.portal.placement.validator;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.placement.PortalBumper;
import io.github.fusionflux.portalcubed.content.portal.placement.PortalPlacement;
import io.github.fusionflux.portalcubed.framework.util.Angle;
import io.github.fusionflux.portalcubed.framework.util.Maath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;

public record StandardPortalValidator(Angle rotation) implements PortalValidator {
	public static final MapCodec<StandardPortalValidator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Angle.CODEC.fieldOf("rotation").forGetter(StandardPortalValidator::rotation)
	).apply(i, StandardPortalValidator::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, StandardPortalValidator> STREAM_CODEC = StreamCodec.composite(
			Angle.STREAM_CODEC, StandardPortalValidator::rotation,
			StandardPortalValidator::new
	);

	public static final PortalValidator.Type<?> TYPE = new Type<>(CODEC, STREAM_CODEC, StandardPortalValidator::parse);

	private static final FloatArgumentType dummyFloat = FloatArgumentType.floatArg(0, 360);

	@Override
	public boolean isValid(ServerLevel level, PortalId id, Portal portal) {
		Direction face = Direction.getApproximateNearest(portal.normal);
		BlockPos pos = BlockPos.containing(portal.data.origin().relative(face, -1e-3));

		PortalPlacement placement = PortalBumper.findValidPlacement(id, level, portal.data.origin(), 0, pos, face, null, this.rotation);

		return placement != null && placement.pos().equals(portal.data.origin()) && Maath.equals(placement.rotation(), portal.rotation(), 1e-5f);
	}

	@Override
	public Type<?> type() {
		return TYPE;
	}

	@Override
	public String toString() {
		return "standard{rotation=" + this.rotation + '}';
	}

	private static PortalValidator.Parsed parse(StringReader reader) throws CommandSyntaxException {
		reader.skipWhitespace();
		Angle rotation = Angle.ofDeg(dummyFloat.parse(reader));
		return ctx -> new StandardPortalValidator(rotation);
	}
}
