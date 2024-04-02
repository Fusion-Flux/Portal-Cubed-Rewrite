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
		Optional<BlockPos> selectedPos,
		boolean preview,
		boolean replaceMode
) {
	public static final String NBT_KEY = "cannon_settings";

	public static final Codec<CannonSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TagKey.codec(Registries.ITEM).optionalFieldOf("material").forGetter(CannonSettings::material),
			ResourceLocation.CODEC.optionalFieldOf("construct").forGetter(CannonSettings::construct),
			PlacementMode.CODEC.fieldOf("placement_mode").forGetter(CannonSettings::mode),
			BlockPos.CODEC.optionalFieldOf("selected_pos").forGetter(CannonSettings::selectedPos),
			Codec.BOOL.fieldOf("preview").forGetter(CannonSettings::preview),
			Codec.BOOL.fieldOf("replace_mode").forGetter(CannonSettings::replaceMode)
	).apply(instance, CannonSettings::new));

	public static final CannonSettings DEFAULT = new CannonSettings(
			Optional.empty(), Optional.empty(), PlacementMode.WHOLE, Optional.empty(), false, true
	);

	@Nullable
	public Configured validate() {
        if (this.material.isPresent() && this.construct.isPresent()) {
			return new Configured(
					this.material.get(), this.construct.get(), this.mode, this.selectedPos.orElse(null)
			);
		}
		return null;
    }

	public CannonSettings withConstruct(ResourceLocation construct) {
		return new CannonSettings(this.material, Optional.ofNullable(construct), this.mode, this.selectedPos, this.preview, this.replaceMode);
	}

	public CannonSettings withMaterial(TagKey<Item> tag) {
		return new CannonSettings(Optional.ofNullable(tag), Optional.empty(), this.mode, this.selectedPos, this.preview, this.replaceMode);
	}

	public CannonSettings withPreview(boolean preview) {
		return new CannonSettings(this.material, this.construct, this.mode, this.selectedPos, preview, this.replaceMode);
	}

	public CannonSettings withReplaceMode(boolean replaceMode) {
		return new CannonSettings(this.material, this.construct, this.mode, this.selectedPos, this.preview, replaceMode);
	}

	public record Configured(TagKey<Item> material, ResourceLocation construct, PlacementMode mode, @Nullable BlockPos selected) {
	}
}
