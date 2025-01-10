package io.github.fusionflux.portalcubed.framework.construct.set;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import io.github.fusionflux.portalcubed.framework.gui.util.AdvancedTooltip;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Rotation;

/**
 * A ConstructSet is a set of one or more Constructs placeable by the Construction Cannon.
 */
public abstract class ConstructSet {
	public static final Codec<ConstructSet> CODEC = Type.CODEC.dispatch(
			construct -> construct.type, Type::codec
	);
	public static final StreamCodec<ByteBuf, ConstructSet> STREAM_CODEC = Type.STREAM_CODEC.dispatch(
			construct -> construct.type, Type::streamCodec
	);

	public static final Comparator<ConstructSet> BY_SIZE_COMPARATOR = (a, b) -> {
		// go by size first
		int bySize = Integer.compare(a.preview.blocks.size(), b.preview.blocks.size());
		if (bySize != 0)
			return bySize;

		// use ID as fallback
		ResourceLocation aId = ConstructManager.INSTANCE.getId(a);
		ResourceLocation bId = ConstructManager.INSTANCE.getId(b);
		// sort nulls before non-nulls, just in case
		if (aId == null) {
			return bId == null ? 0 : 1;
		} else if (bId == null) {
			return -1;
		} else {
			return aId.compareTo(bId);
		}
	};

	public final Type type;
	public final TagKey<Item> material;
	public final int cost;
	public final ConfiguredConstruct preview;

	protected ConstructSet(Type type, TagKey<Item> material, int cost, ConfiguredConstruct preview) {
		this.type = type;
		this.material = material;
		this.cost = cost;
		this.preview = preview;
	}

	protected ConstructSet(Type type, TagKey<Item> material, int cost, Construct preview) {
		this(type, material, cost, new ConfiguredConstruct(preview));
	}

	public abstract ConfiguredConstruct choose(ConstructPlacementContext ctx);

	public void appendTooltip(AdvancedTooltip.Builder builder) {
	}

	public static Component getName(ResourceLocation id) {
		String key = "construct_set." + id.toString().replace(':', '.').replace('/', '.');
		return Component.translatable(key);
	}

	public static int getCost(Optional<Integer> cost, Construct construct) {
		return cost.orElseGet(() -> construct.getBlocks(Rotation.NONE).size());
	}

	public enum Type implements StringRepresentable {
		SINGLE(() -> SingleConstructSet.CODEC, () -> SingleConstructSet.STREAM_CODEC),
		PILLAR(() -> PillarConstructSet.CODEC, () -> PillarConstructSet.STREAM_CODEC);

		public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
		public static final StreamCodec<ByteBuf, Type> STREAM_CODEC = PortalCubedStreamCodecs.ofEnum(Type.class);

		private final String name;
		private final Supplier<MapCodec<? extends ConstructSet>> codec;
		private final Supplier<StreamCodec<ByteBuf, ? extends ConstructSet>> streamCodec;

		Type(Supplier<MapCodec<? extends ConstructSet>> codec, Supplier<StreamCodec<ByteBuf, ? extends ConstructSet>> streamCodec) {
			this.name = this.name().toLowerCase(Locale.ROOT);
			this.codec = codec;
			this.streamCodec = streamCodec;
		}

		@Override
		@NotNull
		public String getSerializedName() {
			return this.name;
		}

		public MapCodec<? extends ConstructSet> codec() {
			return this.codec.get();
		}

		public StreamCodec<ByteBuf, ? extends ConstructSet> streamCodec() {
			return this.streamCodec.get();
		}
	}
}
