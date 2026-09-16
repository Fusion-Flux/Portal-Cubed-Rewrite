package io.github.fusionflux.portalcubed.framework.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;

public final class PortalCubedCodecs {
	/// serializes positions as "x,y,z", allows use as map keys
	public static final Codec<BlockPos> BLOCKPOS_STRING = Codec.STRING.comapFlatMap(
			string -> {
				String[] components = string.split(",");
				if (components.length != 3) {
					return DataResult.error(() -> "3 components required");
				}

				return parseInt(components[0]).flatMap(x -> parseInt(components[1]).flatMap(y -> parseInt(components[2]).map(z -> new BlockPos(x, y, z))));
			},
			pos -> pos.getX() + "," + pos.getY() + "," + pos.getZ()
	);

	private PortalCubedCodecs() {}

	/// Create a codec from two others, able to handle two different formats.
	/// The predicate determines when the alternative format should be encoded instead of the standard one.
	public static <T> Codec<T> multiFormat(Codec<T> standard, Codec<T> alternate, Predicate<T> useAlt) {
		return Codec.xor(standard, alternate).xmap(PortalCubedCodecs::join, t -> useAlt.test(t) ? Either.right(t) : Either.left(t));
	}

	/// Codec that can read/write either a single T or a set of them. The set is strict and will fail to decode
	/// when duplicates are present.
	public static <T> Codec<Set<T>> singleOrStrictSetOf(Codec<T> codec) {
		return Codec.xor(
				strictSetOf(codec),
				codec.flatComapMap(Set::of, set -> {
					if (set.size() == 1) {
						return DataResult.success(set.iterator().next());
					} else {
						return DataResult.error(() -> "Set size >1");
					}
				})
		).xmap(
				either -> either.left().or(either::right).orElseThrow(),
				set -> set.size() == 1 ? Either.left(set) : Either.right(set)
		);
	}

	/// Create a codec for a set of T which fails to decode when duplicates are present
	public static <T> Codec<Set<T>> strictSetOf(Codec<T> codec) {
		return codec.listOf().comapFlatMap(
				list -> {
					Set<T> set = new HashSet<>();
					for (T t : list) {
						if (!set.add(t)) {
							return DataResult.error(() -> "Set contains duplicate: " + t);
						}
					}
					return DataResult.success(set);
				},
				ArrayList::new
		);
	}

	// this is needed because generics tend to freak out with validate on RecordCodecBuilders
	public static <T> Codec<T> validate(Codec<T> codec, Function<T, DataResult<T>> verifier) {
		return codec.validate(verifier);
	}

	public static <T> MapCodec<T> validate(MapCodec<T> codec, Function<T, DataResult<T>> verifier) {
		return codec.validate(verifier);
	}

	private static DataResult<Integer> parseInt(String s) {
		try {
			return DataResult.success(Integer.parseInt(s));
		} catch (NumberFormatException e) {
			return DataResult.error(() -> "Not an integer: " + s);
		}
	}

	private static <T> T join(Either<T, T> either) {
		return either.map(Function.identity(), Function.identity());
	}
}
