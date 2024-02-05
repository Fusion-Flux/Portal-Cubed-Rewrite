package io.github.fusionflux.portalcubed.content.cannon.data;

import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record PlacementSettings(
		Optional<Item> material,
		Optional<ResourceLocation> construct,
		PlacementMode mode,
		Optional<BlockPos> selectedPos
) {
	public static final Codec<PlacementSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("material").forGetter(PlacementSettings::material),
			ResourceLocation.CODEC.optionalFieldOf("construct").forGetter(PlacementSettings::construct),
			PlacementMode.CODEC.fieldOf("placement_mode").forGetter(PlacementSettings::mode),
			BlockPos.CODEC.optionalFieldOf("selected_pos").forGetter(PlacementSettings::selectedPos)
	).apply(instance, PlacementSettings::new));

	@Nullable
	public Configured validate() {
		if (this.material.isPresent() && this.construct.isPresent()) {
			return new Configured(this.material.get(), this.construct.get(), this.mode, this.selectedPos.orElse(null));
		}
		return null;
	}

	public record Configured(Item material, ResourceLocation construct, PlacementMode mode, @Nullable BlockPos selected) {
	}
}
