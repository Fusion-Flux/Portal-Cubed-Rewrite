package io.github.fusionflux.portalcubed.content.portal.placement.validator;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.placement.PortalBumper;
import io.github.fusionflux.portalcubed.content.portal.placement.PortalPlacement;
import io.github.fusionflux.portalcubed.framework.command.argument.DirectionArgumentType;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;

public record StandardPortalValidator(BlockPos pos, Direction face, float yRot) implements PortalValidator {
	public static final MapCodec<StandardPortalValidator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			BlockPos.CODEC.fieldOf("pos").forGetter(StandardPortalValidator::pos),
			Direction.CODEC.fieldOf("face").forGetter(StandardPortalValidator::face),
			Codec.FLOAT.fieldOf("y_rot").forGetter(StandardPortalValidator::yRot)
	).apply(i, StandardPortalValidator::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, StandardPortalValidator> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, StandardPortalValidator::pos,
			Direction.STREAM_CODEC, StandardPortalValidator::face,
			ByteBufCodecs.FLOAT, StandardPortalValidator::yRot,
			StandardPortalValidator::new
	);

	public static final PortalValidator.Type<?> TYPE = new Type<>(CODEC, STREAM_CODEC, StandardPortalValidator::parse);

	private static final BlockPosArgument dummyBlockPos = BlockPosArgument.blockPos();
	private static final DirectionArgumentType dummyDirection = DirectionArgumentType.direction();
	private static final FloatArgumentType dummyFloat = FloatArgumentType.floatArg(0, 360);

	@Override
	public boolean isValid(ServerLevel level, PortalInstance.Holder holder) {
		PortalInstance portal = holder.portal();
		PortalPlacement placement = PortalBumper.findValidPlacement(
				holder.asId(), level, portal.data.origin(),
				this.yRot, this.pos, this.face
		);

		return placement != null && placement.pos().equals(portal.data.origin()) && placement.rotation().equals(portal.data.rotation());
	}

	@Override
	public Type<?> type() {
		return TYPE;
	}

	@Override
	public String toString() {
		return "standard{pos=" + this.pos + ", face=" + this.face + ", yRot=" + this.yRot + '}';
	}

	private static PortalValidator.Parsed parse(StringReader reader) throws CommandSyntaxException {
		reader.skipWhitespace();
		Coordinates coords = dummyBlockPos.parse(reader);
		reader.skipWhitespace();
		Direction face = dummyDirection.parse(reader);
		reader.skipWhitespace();
		Float yRot = dummyFloat.parse(reader);
		return ctx -> new StandardPortalValidator(coords.getBlockPos(ctx.getSource()), face, yRot);
	}
}
