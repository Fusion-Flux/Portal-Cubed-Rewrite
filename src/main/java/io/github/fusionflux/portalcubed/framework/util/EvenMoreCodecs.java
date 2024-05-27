package io.github.fusionflux.portalcubed.framework.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import com.mojang.serialization.DataResult;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class EvenMoreCodecs {
	/**
	 * serializes positions as "x,y,z", allows use as map keys
	 */
	public static final Codec<BlockPos> BLOCKPOS_STRING = Codec.STRING.comapFlatMap(
			string -> {
				String[] components = string.split(",");
				if (components.length != 3) {
					return DataResult.error(() -> "3 components required");
				}
				return DataResult.success(new BlockPos(
								Integer.parseInt(components[0]),
								Integer.parseInt(components[1]),
								Integer.parseInt(components[2])
				));
			},
			pos -> pos.getX() + "," + pos.getY() + "," + pos.getZ()
	);

	/**
	 * BlockState by name of block.
	 */
	private static final Codec<BlockState> defaultBlockState = BuiltInRegistries.BLOCK.byNameCodec().flatComapMap(
			Block::defaultBlockState, state -> {
				if (state.getBlock().defaultBlockState() != state) {
					return DataResult.error(() -> "State is not default");
				}
				return DataResult.success(state.getBlock());
			}
	);

	/**
	 * Extended BlockState codec able to handle an additional format.
	 */
	public static final Codec<BlockState> BLOCKSTATE = multiFormat(
			BlockState.CODEC, defaultBlockState, state -> state.getBlock().defaultBlockState() == state
	);

	/**
	 * Create a codec from two others, able to handle two different formats.
	 * The predicate determines when the alternative format should be encoded instead of the standard one.
	 */
	public static <T> Codec<T> multiFormat(Codec<T> standard, Codec<T> alternate, Predicate<T> useAlt) {
		return Codec.either(standard, alternate).comapFlatMap(
				either -> {
					if (either.left().isPresent())
						return DataResult.success(either.left().get());
					if (either.right().isPresent())
						return DataResult.success(either.right().get());
					return DataResult.error(() -> "Both codecs failed");
				},
				t -> useAlt.test(t) ? Either.right(t) : Either.left(t)
		);
	}
}
