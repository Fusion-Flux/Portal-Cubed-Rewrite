package io.github.fusionflux.portalcubed.framework.util;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public interface PortalCubedStreamCodecs {
	StreamCodec<ByteBuf, Unit> UNIT = StreamCodec.unit(Unit.INSTANCE);

	StreamCodec<ByteBuf, InteractionHand> HAND = ByteBufCodecs.BOOL.map(
			value -> value ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
			hand -> hand == InteractionHand.MAIN_HAND
	);

	StreamCodec<ByteBuf, BlockHitResult> BLOCK_HIT_RESULT = StreamCodec.composite(
			ByteBufCodecs.BOOL, i -> i.getType() == HitResult.Type.MISS,
			Vec3.STREAM_CODEC, HitResult::getLocation,
			Direction.STREAM_CODEC, BlockHitResult::getDirection,
			BlockPos.STREAM_CODEC, BlockHitResult::getBlockPos,
			ByteBufCodecs.BOOL, BlockHitResult::isInside,
			(miss, location, direction, blockPos, isInside) ->
					miss ? BlockHitResult.miss(location, direction, blockPos) :
							new BlockHitResult(location, direction, blockPos, isInside)
	);

	StreamCodec<ByteBuf, Quaternionfc> QUATERNIONFC = new StreamCodec<>() {
		@Override
		public Quaternionfc decode(ByteBuf buf) {
			return new Quaternionf(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
		}

		@Override
		public void encode(ByteBuf buf, Quaternionfc quat) {
			buf.writeFloat(quat.x());
			buf.writeFloat(quat.y());
			buf.writeFloat(quat.z());
			buf.writeFloat(quat.w());
		}
	};

	// from EntityDataSerializers
	StreamCodec<ByteBuf, BlockState> BLOCK_STATE = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);

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

	// hides the HashMap, pleases generics
	static <B extends ByteBuf, K, V> StreamCodec<B, Map<K, V>> map(StreamCodec<? super B, K> key, StreamCodec<? super B, V> value) {
		return ByteBufCodecs.map(HashMap::new, key, value);
	}

	static <B extends ByteBuf, V> StreamCodec<B, @Nullable V> nullable(StreamCodec<B, V> base) {
		return new StreamCodec<>() {
			@SuppressWarnings("NullableProblems")
			@Override
			@Nullable
			public V decode(B buf) {
				return FriendlyByteBuf.readNullable(buf, base);
			}

			@Override
			public void encode(B buf, @Nullable V value) {
				FriendlyByteBuf.writeNullable(buf, value, base);
			}
		};
	}
}
