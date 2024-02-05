package io.github.fusionflux.portalcubed.framework.construct;

import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * A construct that always places the same structure.
 */
public class MonoConstruct extends Construct {
	public static Codec<MonoConstruct> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("material").forGetter(c -> c.material),
			ResourceLocation.CODEC.fieldOf("structure").forGetter(c -> c.structure)
	).apply(instance, MonoConstruct::new));

	private final ResourceLocation structure;

	public MonoConstruct(Item material, ResourceLocation structure) {
		super(Type.MONO, material);
		this.structure = structure;
	}

	@Override
	public ResourceLocation getStructure(ConstructPlacementContext ctx) {
		return this.structure;
	}
}
