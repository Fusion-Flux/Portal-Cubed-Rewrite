package io.github.fusionflux.portalcubed.framework.util;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EvenMoreCodecs {
	/**
	 * {@link ResourceLocation#CODEC} but the default namespace is {@value PortalCubed#ID}
	 */
	public static final Codec<ResourceLocation> MOD_ID = Codec.STRING.comapFlatMap(id -> {
		try {
			String namespace = PortalCubed.ID;
			String path = id;
			int separator = id.indexOf(ResourceLocation.NAMESPACE_SEPARATOR);
			if (separator >= 0) {
				namespace = id.substring(separator + 1);
				if (separator >= 1) {
					path = id.substring(0, separator);
				}
			}
			return DataResult.success(ResourceLocation.fromNamespaceAndPath(namespace, path));
		} catch (ResourceLocationException e) {
			return DataResult.error(() -> "Not a valid resource location: " + id + " " + e.getMessage());
		}
	}, ResourceLocation::toString).stable();

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

	public static <K, V> Codec<Multimap<K, V>> unboundedMultimap(Codec<K> keyCodec, Codec<V> elementCodec) {
		return Codec.compoundList(
				keyCodec,
				Codec.either(elementCodec, ExtraCodecs.nonEmptyList(elementCodec.listOf()))
		).xmap(
				list -> {
					Multimap<K, V> result = ArrayListMultimap.create();
					list.forEach(p -> p.getSecond()
							.map(
									s -> result.put(p.getFirst(), s),
									l -> result.putAll(p.getFirst(), l)
							)
					);
					return result;
				},
				multimap -> multimap.asMap().entrySet().stream()
						.map(e -> Pair.of(
								e.getKey(),
								Either.<V, List<V>>right(List.copyOf(e.getValue()))
						))
						.toList()
		);
	}

	// this is needed because generics tend to freak out with validate on RecordCodecBuilders
	public static <T> Codec<T> validate(Codec<T> codec, Function<T, DataResult<T>> verifier) {
		return codec.validate(verifier);
	}
	public static <T> MapCodec<T> validate(MapCodec<T> codec, Function<T, DataResult<T>> verifier) {
		return codec.validate(verifier);
	}
}
