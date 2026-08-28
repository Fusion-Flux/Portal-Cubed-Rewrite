package io.github.fusionflux.portalcubed.framework.model.emissive;

import java.util.Collection;

import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.framework.util.PortalCubedCodecs;
import net.minecraft.resources.Identifier;

public record EmissiveData(Multimap<Identifier, EmissiveTexturePredicate> map) {
	public static final Codec<EmissiveData> CODEC = PortalCubedCodecs.unboundedMultimap(Identifier.CODEC, EmissiveTexturePredicate.CODEC)
			.xmap(EmissiveData::new, EmissiveData::map);

	public Collection<EmissiveTexturePredicate> predicatesForModel(Identifier id) {
		return this.map.get(id);
	}
}
