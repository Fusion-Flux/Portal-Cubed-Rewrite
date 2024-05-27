package io.github.fusionflux.portalcubed.framework.construct.set;

import java.util.Optional;

import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

/**
 * A construct that always places the same structure.
 */
public class SingleConstructSet extends ConstructSet {
	public static Codec<SingleConstructSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TagKey.hashedCodec(Registries.ITEM).fieldOf("material").forGetter(c -> c.material),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("cost").forGetter(c -> Optional.of(c.cost)),
			Construct.CODEC.fieldOf("construct").forGetter(c -> c.construct)
	).apply(instance, SingleConstructSet::new));

	private final Construct construct;

	public SingleConstructSet(TagKey<Item> material, Optional<Integer> cost, Construct construct) {
		super(Type.SINGLE, material, ConstructSet.getCost(cost, construct), construct);
		this.construct = construct;
	}

	@Override
	public ConfiguredConstruct choose(ConstructPlacementContext ctx) {
		return new ConfiguredConstruct(this.construct);
	}
}
