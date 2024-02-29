package io.github.fusionflux.portalcubed.framework.construct.set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * A construct set that places one of two constructs based on the axis of the clicked face.
 */
public class DualFaceConstructSet extends ConstructSet {
	public static Codec<DualFaceConstructSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TagKey.hashedCodec(Registries.ITEM).fieldOf("material").forGetter(c -> c.material),
			Construct.CODEC.fieldOf("horizontal").forGetter(c -> c.horizontal),
			Construct.CODEC.fieldOf("vertical").forGetter(c -> c.vertical)
	).apply(instance, DualFaceConstructSet::new));

	private final Construct horizontal;
	private final Construct vertical;

	public DualFaceConstructSet(TagKey<Item> material, Construct horizontal, Construct vertical) {
		super(Type.DUAL_FACE, material);
		this.horizontal = horizontal;
		this.vertical = vertical;
	}

	@Override
	public Construct choose(ConstructPlacementContext ctx) {
		return ctx.clickedFace().getAxis().isVertical() ? this.vertical : this.horizontal;
	}

	@Override
	public Construct getDefault() {
		return this.horizontal;
	}
}
