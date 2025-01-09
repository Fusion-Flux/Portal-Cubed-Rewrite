package io.github.fusionflux.portalcubed.content.cannon;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public record CannonSettings(
		Optional<TagKey<Item>> material,
		Optional<ResourceLocation> construct,
		boolean preview,
		float previewOpacity,
		boolean replaceMode
) {
	public static final String NBT_KEY = "cannon_settings";

	public static final Codec<CannonSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TagKey.codec(Registries.ITEM).optionalFieldOf("material").forGetter(CannonSettings::material),
			ResourceLocation.CODEC.optionalFieldOf("construct").forGetter(CannonSettings::construct),
			Codec.BOOL.fieldOf("preview").forGetter(CannonSettings::preview),
			Codec.FLOAT.fieldOf("preview_opacity").forGetter(CannonSettings::previewOpacity),
			Codec.BOOL.fieldOf("replace_mode").forGetter(CannonSettings::replaceMode)
	).apply(instance, CannonSettings::new));

	public static final CannonSettings DEFAULT = new CannonSettings(
			Optional.empty(), Optional.empty(), true, .55f, false
	);

	@Nullable
	public Configured validate() {
		return this.material.flatMap(
				material -> this.construct.map(ConstructManager.INSTANCE::getConstructSet)
						.map(set -> new Configured(material, set))
		).orElse(null);
    }

	public CannonSettings withConstruct(ResourceLocation construct) {
		return new CannonSettings(this.material, Optional.ofNullable(construct), this.preview, this.previewOpacity, this.replaceMode);
	}

	public CannonSettings withMaterial(TagKey<Item> tag) {
		return new CannonSettings(Optional.ofNullable(tag), Optional.empty(), this.preview, this.previewOpacity, this.replaceMode);
	}

	public CannonSettings withPreview(boolean preview) {
		return new CannonSettings(this.material, this.construct, preview, this.previewOpacity, this.replaceMode);
	}

	public CannonSettings withPreviewOpacity(float previewOpacity) {
		return new CannonSettings(this.material, this.construct, this.preview, previewOpacity, this.replaceMode);
	}

	public CannonSettings withReplaceMode(boolean replaceMode) {
		return new CannonSettings(this.material, this.construct, this.preview, this.previewOpacity, replaceMode);
	}

	public record Configured(TagKey<Item> material, ConstructSet construct) {
	}
}
