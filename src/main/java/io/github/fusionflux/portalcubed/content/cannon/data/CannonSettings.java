package io.github.fusionflux.portalcubed.content.cannon.data;

import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record CannonSettings(
		Optional<ResourceLocation> construct,
		PlacementMode mode,
		Optional<BlockPos> selectedPos
) {
	public static final String NBT_KEY = "cannon_settings";

	public static final Codec<CannonSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("construct").forGetter(CannonSettings::construct),
			PlacementMode.CODEC.fieldOf("placement_mode").forGetter(CannonSettings::mode),
			BlockPos.CODEC.optionalFieldOf("selected_pos").forGetter(CannonSettings::selectedPos)
	).apply(instance, CannonSettings::new));

	public static final CannonSettings DEFAULT = new CannonSettings(
			Optional.empty(), PlacementMode.WHOLE, Optional.empty()
	);

	@Nullable
	public Configured validate() {
        return this.construct.map(
				id -> new Configured(id, this.mode, this.selectedPos.orElse(null))
		).orElse(null);
    }

	public CannonSettings withConstruct(Optional<ResourceLocation> construct) {
		return new CannonSettings(construct, this.mode, this.selectedPos);
	}

	public record Configured(ResourceLocation construct, PlacementMode mode, @Nullable BlockPos selected) {
	}
}
