package io.github.fusionflux.portalcubed.framework.construct.set;

import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

/**
 * A construct that always places the same structure.
 */
public class MonoConstructSet extends ConstructSet {
	public static Codec<MonoConstructSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("material").forGetter(c -> c.material),
			Construct.CODEC.fieldOf("construct").forGetter(c -> c.construct)
	).apply(instance, MonoConstructSet::new));

	private final Construct construct;

	public MonoConstructSet(Item material, Construct construct) {
		super(Type.MONO, material);
		this.construct = construct;
	}

	@Override
	public Construct choose(ConstructPlacementContext ctx) {
		return this.construct;
	}
}
