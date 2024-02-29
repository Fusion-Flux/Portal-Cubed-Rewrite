package io.github.fusionflux.portalcubed.content.cannon.data;

import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record CannonSettings(
		Optional<TagKey<Item>> material,
		Optional<ResourceLocation> construct,
		PlacementMode mode,
		Optional<BlockPos> selectedPos
) {
	public static final String NBT_KEY = "cannon_settings";

	public static final Codec<CannonSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TagKey.codec(Registries.ITEM).optionalFieldOf("material").forGetter(CannonSettings::material),
			ResourceLocation.CODEC.optionalFieldOf("construct").forGetter(CannonSettings::construct),
			PlacementMode.CODEC.fieldOf("placement_mode").forGetter(CannonSettings::mode),
			BlockPos.CODEC.optionalFieldOf("selected_pos").forGetter(CannonSettings::selectedPos)
	).apply(instance, CannonSettings::new));

	public static final CannonSettings DEFAULT = new CannonSettings(
			Optional.empty(), Optional.empty(), PlacementMode.WHOLE, Optional.empty()
	);

	@Nullable
	public Configured validate() {
		if (this.material.isPresent() && this.construct.isPresent()) {
			return new Configured(this.material.get(), this.construct.get(), this.mode, this.selectedPos.orElse(null));
		}
		return null;
	}

	public CannonSettings withMaterial(TagKey<Item> material) {
		return new CannonSettings(Optional.of(material), this.construct, this.mode, this.selectedPos);
	}

	public CannonSettings withConstruct(Optional<ResourceLocation> construct) {
		return new CannonSettings(this.material, construct, this.mode, this.selectedPos);
	}

	public record Configured(TagKey<Item> material, ResourceLocation construct, PlacementMode mode, @Nullable BlockPos selected) {
	}
}
