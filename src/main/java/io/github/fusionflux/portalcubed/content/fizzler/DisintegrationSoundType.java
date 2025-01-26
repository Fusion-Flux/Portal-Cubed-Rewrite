package io.github.fusionflux.portalcubed.content.fizzler;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record DisintegrationSoundType(HolderSet<EntityType<?>> entities, List<ResourceLocation> sounds) {
	public static final Codec<DisintegrationSoundType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(DisintegrationSoundType::entities),
			ExtraCodecs.compactListCodec(ResourceLocation.CODEC).fieldOf("sounds").forGetter(DisintegrationSoundType::sounds)
	).apply(instance, DisintegrationSoundType::new));
	public static final ResourceKey<DisintegrationSoundType> GENERIC_KEY = ResourceKey.create(PortalCubedRegistries.DISINTEGRATION_SOUND_TYPE, PortalCubed.id("generic"));

	public static DisintegrationSoundType lookup(Entity entity) {
		HolderLookup<DisintegrationSoundType> registryLookup = entity.registryAccess().lookupOrThrow(PortalCubedRegistries.DISINTEGRATION_SOUND_TYPE);
		return registryLookup
				.listElements()
				.map(Holder::value)
				.filter(type -> entity.getType().is(type.entities))
				.findFirst()
				.orElse(registryLookup.getOrThrow(GENERIC_KEY).value());
	}

	@Environment(EnvType.CLIENT)
	public static void playAll(Entity entity) {
		if (entity.isSilent())
			return;

		DisintegrationSoundType type = lookup(entity);
		for (ResourceLocation sound : type.sounds) {
			entity.level().pc$playSoundInstance(new FollowingSoundInstance(SoundEvent.createVariableRangeEvent(sound), entity.getSoundSource(), entity, false));
		}
	}
}
