package io.github.fusionflux.portalcubed.framework.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public interface PortalCubedStreamCodecs {
	StreamCodec<ByteBuf, Unit> UNIT = StreamCodec.unit(Unit.INSTANCE);

	StreamCodec<ByteBuf, Vec3> VEC3 = new StreamCodec<>() {
		@Override
		public @NotNull Vec3 decode(ByteBuf buffer) {
			return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		}

		@Override
		public void encode(ByteBuf buffer, Vec3 value) {
			buffer.writeDouble(value.x);
			buffer.writeDouble(value.y);
			buffer.writeDouble(value.z);
		}
	};

	StreamCodec<ByteBuf, InteractionHand> HAND = ByteBufCodecs.BOOL.map(
			value -> value ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
			hand -> hand == InteractionHand.MAIN_HAND
	);

	StreamCodec<ByteBuf, BlockHitResult> BLOCK_HIT_RESULT = StreamCodec.composite(
			ByteBufCodecs.BOOL, i -> i.getType() == HitResult.Type.MISS,
			VEC3, HitResult::getLocation,
			Direction.STREAM_CODEC, BlockHitResult::getDirection,
			BlockPos.STREAM_CODEC, BlockHitResult::getBlockPos,
			ByteBufCodecs.BOOL, BlockHitResult::isInside,
			(miss, location, direction, blockPos, isInside) ->
					miss ? BlockHitResult.miss(location, direction, blockPos) :
							new BlockHitResult(location, direction, blockPos, isInside)
	);

	static <T extends Enum<T>> StreamCodec<ByteBuf, T> ofEnum(Class<? extends T> clazz) {
		T[] values = clazz.getEnumConstants();
		return new StreamCodec<>() {
			@Override
			public T decode(ByteBuf buf) {
				return values[VarInt.read(buf)];
			}

			@Override
			public void encode(ByteBuf buf, T value) {
				VarInt.write(buf, value.ordinal());
			}
		};
	}

	static <B extends ByteBuf, V> StreamCodec<B, @Nullable V> nullable(StreamCodec<B, V> base) {
		return new StreamCodec<>() {
			@Override
			@SuppressWarnings("NullableProblems")
			public @Nullable V decode(@NotNull B buffer) {
				if (buffer.readBoolean())
					return base.decode(buffer);
				else
					return null;
			}

			@Override
			public void encode(@NotNull B buffer, @Nullable V value) {
				if (value != null) {
					buffer.writeBoolean(true);
					base.encode(buffer, value);
				} else {
					buffer.writeBoolean(false);
				}
			}
		};
	}
}
