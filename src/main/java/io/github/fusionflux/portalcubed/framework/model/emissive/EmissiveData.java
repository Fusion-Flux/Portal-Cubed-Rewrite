package io.github.fusionflux.portalcubed.framework.model.emissive;

import java.util.Collection;

import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.framework.util.EvenMoreCodecs;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public record EmissiveData(Multimap<ResourceLocation, EmissiveTexturePredicate> map) {
	public static final Codec<EmissiveData> CODEC = EvenMoreCodecs.unboundedMultimap(EvenMoreCodecs.MOD_ID, EmissiveTexturePredicate.CODEC)
			.xmap(EmissiveData::new, EmissiveData::map);

	public Collection<EmissiveTexturePredicate> predicatesForModel(ResourceLocation id) {
		if (id instanceof ModelResourceLocation modelId && modelId.getVariant().equals("inventory")) {
			ResourceLocation sourceFormat = modelId.withPrefix("item/"); // also removes variant
			return map.get(sourceFormat);
		}
		return map.get(id);
	}
}
