package io.github.fusionflux.portalcubed.content.portal.gun.skin;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;

public record PortalGunSkin(Component name, ResourceLocation itemModel, Sounds sounds) {
	public static final Codec<PortalGunSkin> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ComponentSerialization.CODEC.fieldOf("name").forGetter(PortalGunSkin::name),
			ResourceLocation.CODEC.fieldOf("item_model").forGetter(PortalGunSkin::itemModel),
			Sounds.CODEC.fieldOf("sounds").forGetter(PortalGunSkin::sounds)
	).apply(instance, PortalGunSkin::new));
	public static final ResourceKey<Registry<PortalGunSkin>> REGISTRY_KEY = ResourceKey.createRegistryKey(PortalCubed.id("portal_gun_skin"));
	public static final ResourceKey<PortalGunSkin> DEFAULT = ResourceKey.create(REGISTRY_KEY, PortalCubed.id("default"));

	public record Sounds(
			Optional<Holder<SoundEvent>> primaryShoot,
			Optional<Holder<SoundEvent>> secondaryShoot,
			Optional<Holder<SoundEvent>> fizzle,
			Optional<Grab> grab,
			Optional<Holder<SoundEvent>> release,
			Optional<Holder<SoundEvent>> cannotGrab,
			Optional<Holder<SoundEvent>> holdLoop
	) {
		public static final Codec<Sounds> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				SoundEvent.CODEC.optionalFieldOf("primary_shoot").forGetter(Sounds::primaryShoot),
				SoundEvent.CODEC.optionalFieldOf("secondary_shoot").forGetter(Sounds::secondaryShoot),
				SoundEvent.CODEC.optionalFieldOf("fizzle").forGetter(Sounds::fizzle),
				Grab.CODEC.optionalFieldOf("grab").forGetter(Sounds::grab),
				SoundEvent.CODEC.optionalFieldOf("release").forGetter(Sounds::release),
				SoundEvent.CODEC.optionalFieldOf("cannot_grab").forGetter(Sounds::cannotGrab),
				SoundEvent.CODEC.optionalFieldOf("hold_loop").forGetter(Sounds::holdLoop)
		).apply(instance, Sounds::new));
		public static final Sounds EMPTY = new Sounds(
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.empty()
		);

		public Optional<Holder<SoundEvent>> shootOf(Polarity polarity) {
			return polarity == Polarity.PRIMARY ? this.primaryShoot : this.secondaryShoot;
		}

		public record Grab(Holder<SoundEvent> sound, int lengthInTicks) {
			public static final Codec<Grab> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					SoundEvent.CODEC.fieldOf("sound").forGetter(Grab::sound),
					ExtraCodecs.POSITIVE_INT.fieldOf("length").forGetter(Grab::lengthInTicks)
			).apply(instance, Grab::new));
		}
	}
}
