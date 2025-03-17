package io.github.fusionflux.portalcubed.framework.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.PortalCubed;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface PortalCubedCodecs {
	/**
	 * {@link ResourceLocation#CODEC} but the default namespace is {@value PortalCubed#ID}
	 */
	Codec<ResourceLocation> MOD_ID = Codec.STRING.comapFlatMap(id -> {
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
	Codec<BlockPos> BLOCKPOS_STRING = Codec.STRING.comapFlatMap(
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
	Codec<BlockState> defaultBlockState = BuiltInRegistries.BLOCK.byNameCodec().flatComapMap(
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
	Codec<BlockState> BLOCKSTATE = multiFormat(
			BlockState.CODEC, defaultBlockState, state -> state.getBlock().defaultBlockState() == state
	);

	@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
	Codec<Quaternionfc> QUATERNIONFC = Codec.withAlternative(
			Codec.FLOAT
					.listOf()
					.comapFlatMap(
							list -> Util.fixedSize(list, 4)
									.map(listx -> new Quaternionf(listx.get(0), listx.get(1), listx.get(2), listx.get(3))),
							quat -> FloatArrayList.of(quat.x(), quat.y(), quat.z(), quat.w())
					),
			ExtraCodecs.AXISANGLE4F.xmap(Quaternionf::new, AxisAngle4f::new)
	);

	/**
	 * Create a codec from two others, able to handle two different formats.
	 * The predicate determines when the alternative format should be encoded instead of the standard one.
	 */
	static <T> Codec<T> multiFormat(Codec<T> standard, Codec<T> alternate, Predicate<T> useAlt) {
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

	static <K, V> Codec<Multimap<K, V>> unboundedMultimap(Codec<K> keyCodec, Codec<V> elementCodec) {
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

	/**
	 * Codec that can read/write either a single T or a set of them. The set is strict and will fail to decode
	 * when duplicates are present.
	 */
	static <T> Codec<Set<T>> singleOrStrictSetOf(Codec<T> codec) {
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

	/**
	 * Create a codec for a set of T which fails to decode when duplicates are present
	 */
	static <T> Codec<Set<T>> strictSetOf(Codec<T> codec) {
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
	static <T> Codec<T> validate(Codec<T> codec, Function<T, DataResult<T>> verifier) {
		return codec.validate(verifier);
	}
	static <T> MapCodec<T> validate(MapCodec<T> codec, Function<T, DataResult<T>> verifier) {
		return codec.validate(verifier);
	}
}
