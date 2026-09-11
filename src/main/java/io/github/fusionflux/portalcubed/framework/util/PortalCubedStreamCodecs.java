package io.github.fusionflux.portalcubed.framework.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface PortalCubedStreamCodecs {
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

	static <B extends ByteBuf, V> StreamCodec<B, Set<V>> set(StreamCodec<B, V> base) {
		return base.apply(ByteBufCodecs.list()).map(HashSet::new, ArrayList::new);
	}
}
