package io.github.fusionflux.portalcubed.framework.construct.set;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

/**
 * A construct that always places the same structure.
 */
public class SingleConstructSet extends ConstructSet {
	public static final MapCodec<SingleConstructSet> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			TagKey.hashedCodec(Registries.ITEM).fieldOf("material").forGetter(c -> c.material),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("cost").forGetter(c -> Optional.of(c.cost)),
			Construct.CODEC.fieldOf("construct").forGetter(c -> c.construct)
	).apply(instance, SingleConstructSet::new));

	public static final StreamCodec<ByteBuf, SingleConstructSet> STREAM_CODEC = StreamCodec.composite(
			TagKey.streamCodec(Registries.ITEM), set -> set.material,
			ByteBufCodecs.VAR_INT, set -> set.cost,
			Construct.STREAM_CODEC, set -> set.construct,
			SingleConstructSet::new
	);

	private final Construct construct;

	public SingleConstructSet(TagKey<Item> material, Optional<Integer> cost, Construct construct) {
		this(material, ConstructSet.getCost(cost, construct), construct);
	}

	private SingleConstructSet(TagKey<Item> material, int cost, Construct construct) {
		super(Type.SINGLE, material, cost, construct);
		this.construct = construct;
	}

	@Override
	public ConfiguredConstruct choose(ConstructPlacementContext ctx) {
		return new ConfiguredConstruct(this.construct);
	}
}
