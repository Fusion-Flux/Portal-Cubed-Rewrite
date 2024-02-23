package io.github.fusionflux.portalcubed.framework.construct.set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * A construct set that places one construct on Y axis faces, and another one otherwise.
 */
public class BiConstructSet extends ConstructSet {
	public static Codec<BiConstructSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("material").forGetter(c -> c.material),
			Construct.CODEC.fieldOf("horizontal").forGetter(c -> c.horizontal),
			Construct.CODEC.fieldOf("vertical").forGetter(c -> c.vertical)
	).apply(instance, BiConstructSet::new));

	private final Construct horizontal;
	private final Construct vertical;

	public BiConstructSet(Item material, Construct horizontal, Construct vertical) {
		super(Type.BI, material);
		this.horizontal = horizontal;
		this.vertical = vertical;
	}

	@Override
	public Construct choose(ConstructPlacementContext ctx) {
		return ctx.clickedFace().getAxis().isVertical() ? this.vertical : this.horizontal;
	}
}
