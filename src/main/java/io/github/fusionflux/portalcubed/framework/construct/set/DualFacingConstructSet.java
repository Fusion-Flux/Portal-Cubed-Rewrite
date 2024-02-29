package io.github.fusionflux.portalcubed.framework.construct.set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * A construct set that places one of two constructs based on the facing direction of the placer.
 */
public class DualFacingConstructSet extends ConstructSet {
	public static Codec<DualFacingConstructSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TagKey.hashedCodec(Registries.ITEM).fieldOf("material").forGetter(c -> c.material),
			Construct.CODEC.fieldOf("horizontal").forGetter(c -> c.horizontal),
			Construct.CODEC.fieldOf("vertical").forGetter(c -> c.vertical)
	).apply(instance, DualFacingConstructSet::new));

	private final Construct horizontal;
	private final Construct vertical;

	public DualFacingConstructSet(TagKey<Item> material, Construct horizontal, Construct vertical) {
		super(Type.DUAL_FACING, material);
		this.horizontal = horizontal;
		this.vertical = vertical;
	}

	@Override
	public Construct choose(ConstructPlacementContext ctx) {
		return ctx.placerFacing().getAxis().isVertical() ? this.vertical : this.horizontal;
	}
}
