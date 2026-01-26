package io.github.fusionflux.portalcubed.content.portal.advancements;

import java.util.Optional;
import java.util.function.Predicate;

import org.joml.Quaternionf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import io.github.fusionflux.portalcubed.content.portal.graphics.color.PortalColor;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedCodecs;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;

public record PortalPredicate(
		Optional<String> pair, Optional<Polarity> polarity, Optional<HolderSet<PortalType>> types,
		Optional<Vec3> origin, Optional<Quaternionf> rotation, Optional<PortalColor> color
) implements Predicate<PortalReference> {
	public static final Codec<PortalPredicate> CODEC = PortalCubedCodecs.validate(
			RecordCodecBuilder.create(i -> i.group(
					Codec.STRING.optionalFieldOf("pair").forGetter(PortalPredicate::pair),
					Polarity.CODEC.optionalFieldOf("polarity").forGetter(PortalPredicate::polarity),
					RegistryCodecs.homogeneousList(PortalCubedRegistries.PORTAL_TYPE).optionalFieldOf("types").forGetter(PortalPredicate::types),
					Vec3.CODEC.optionalFieldOf("origin").forGetter(PortalPredicate::origin),
					ExtraCodecs.QUATERNIONF.optionalFieldOf("rotation").forGetter(PortalPredicate::rotation),
					PortalColor.CODEC.optionalFieldOf("color").forGetter(PortalPredicate::color)
			).apply(i, PortalPredicate::new)),
			PortalPredicate::validate
	);

	@Override
	public boolean test(PortalReference portal) {
		if (this.pair.isPresent() && !this.pair.get().equals(portal.id.key()))
			return false;

		if (this.polarity.isPresent() && this.polarity.get() != portal.id.polarity())
			return false;

		PortalData data = portal.get().data;

		if (this.types.isPresent() && !this.types.get().contains(data.type()))
			return false;

		if (this.origin.isPresent() && !this.origin.get().equals(data.origin()))
			return false;

		if (this.rotation.isPresent() && !this.rotation.get().equals(data.rotation()))
			return false;

		return this.color.isEmpty() || this.color.get().equals(data.color());
	}

	private DataResult<PortalPredicate> validate() {
		if (this.pair.isEmpty() && this.polarity.isEmpty() && this.types.isEmpty()
				&& this.origin.isEmpty() && this.rotation.isEmpty() && this.color.isEmpty()) {
			return DataResult.error(() -> "PortalPredicate may not be empty");
		}

		return DataResult.success(this);
	}
}
