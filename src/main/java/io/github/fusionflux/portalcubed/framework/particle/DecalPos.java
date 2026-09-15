package io.github.fusionflux.portalcubed.framework.particle;

import org.apache.commons.lang3.Validate;

import io.github.fusionflux.portalcubed.content.misc.DecalParticle;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/// A position of a [DecalParticle] in-world.
/// @param base the base [BlockPos] the decal is attached to
/// @param face the direction the decal faces
/// @param offset the offset along the facing axis where the decal is placed, 0 - 1
public record DecalPos(BlockPos base, Direction face, double offset, int pixelX, int pixelY) {
	public static final double ONE_PIXEL = 1 / 16d;
	public static final double HALF_PIXEL = 1 / 32d;

	public static final StreamCodec<ByteBuf, DecalPos> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, DecalPos::base,
			Direction.STREAM_CODEC, DecalPos::face,
			ByteBufCodecs.DOUBLE, DecalPos::offset,
			ByteBufCodecs.VAR_INT, DecalPos::pixelX,
			ByteBufCodecs.VAR_INT, DecalPos::pixelY,
			DecalPos::new
	);

	public DecalPos {
		Validate.inclusiveBetween(0, 1, offset, "Offset must be between 0 and 1: " + offset);
		Validate.inclusiveBetween(0, 15, pixelX, "X must be between 0 and 15: " + pixelX);
		Validate.inclusiveBetween(0, 15, pixelY, "Y must be between 0 and 15: " + pixelY);
	}

	/// Resolve this position as a [Vec3].
	public Vec3 resolve() {
		Vec3 offsetAlongFacingAxis = getPositiveNormal(this.face.getAxis()).scale(this.offset);
		Vec3 offsetAlongPixelX = getPositiveNormal(getPixelXAxis(this.face)).scale((this.pixelX * ONE_PIXEL) + HALF_PIXEL);
		Vec3 offsetAlongPixelY = getPositiveNormal(getPixelYAxis(this.face)).scale((this.pixelY * ONE_PIXEL) + HALF_PIXEL);
		return Vec3.atLowerCornerOf(this.base).add(offsetAlongFacingAxis).add(offsetAlongPixelX).add(offsetAlongPixelY);
	}

	/// Create a DecalPos corresponding to the given position and facing direction.
	/// The position will be pixel-aligned along the two non-facing axes.
	public static DecalPos of(Vec3 pos, Direction face) {
		Vec3 offsetIntoBlock = face.getUnitVec3().scale(-HALF_PIXEL);
		BlockPos base = BlockPos.containing(pos.add(offsetIntoBlock));

		Vec3 relativeToCorner = Vec3.atLowerCornerOf(base).vectorTo(pos);
		double offset = relativeToCorner.get(face.getAxis());

		int pixelX = Mth.floor(relativeToCorner.get(getPixelXAxis(face)) * 16);
		int pixelY = Mth.floor(relativeToCorner.get(getPixelYAxis(face)) * 16);

		return new DecalPos(base, face, offset, pixelX, pixelY);
	}

	private static Direction.Axis getPixelXAxis(Direction facing) {
		return switch (facing) {
			case EAST, WEST -> Direction.Axis.Z;
			default -> Direction.Axis.X;
		};
	}

	private static Direction.Axis getPixelYAxis(Direction facing) {
		return switch (facing) {
			case UP, DOWN -> Direction.Axis.Z;
			default -> Direction.Axis.Y;
		};
	}

	private static Vec3 getPositiveNormal(Direction.Axis axis) {
		return axis.getPositive().getUnitVec3();
	}
}
