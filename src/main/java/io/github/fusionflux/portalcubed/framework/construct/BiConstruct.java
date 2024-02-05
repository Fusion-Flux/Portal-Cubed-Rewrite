package io.github.fusionflux.portalcubed.framework.construct;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * A construct that places one structure on Y axis faces, and another one otherwise.
 */
public class BiConstruct extends Construct {
	public static Codec<BiConstruct> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("material").forGetter(c -> c.material),
			ResourceLocation.CODEC.fieldOf("horizontal").forGetter(c -> c.horizontal),
			ResourceLocation.CODEC.fieldOf("vertical").forGetter(c -> c.vertical)
	).apply(instance, BiConstruct::new));

	private final ResourceLocation horizontal;
	private final ResourceLocation vertical;

	public BiConstruct(Item material, ResourceLocation horizontal, ResourceLocation vertical) {
		super(Type.BI, material);
		this.horizontal = horizontal;
		this.vertical = vertical;
	}

	@Override
	public ResourceLocation getStructure(ConstructPlacementContext ctx) {
		return ctx.clickedFace().getAxis().isVertical() ? this.vertical : this.horizontal;
	}
}
