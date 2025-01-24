package io.github.fusionflux.portalcubed.framework.signage;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record Signage(Optional<ResourceLocation> cleanTexture, Optional<ResourceLocation> agedTexture, Component name) {
	public static final Codec<Signage> DIRECT_CODEC = PortalCubedCodecs.validate(
			RecordCodecBuilder.create(instance -> instance.group(
					ResourceLocation.CODEC.optionalFieldOf("clean_texture").forGetter(Signage::cleanTexture),
					ResourceLocation.CODEC.optionalFieldOf("aged_texture").forGetter(Signage::agedTexture),
					ComponentSerialization.CODEC.fieldOf("name").forGetter(Signage::name)
			).apply(instance, Signage::new)),
			Signage::validate
	);

	public ResourceLocation selectTexture(boolean aged) {
		if (this.cleanTexture.isPresent() && !aged)
			return this.cleanTexture.get();
		if (this.agedTexture.isPresent() && aged)
			return this.agedTexture.get();
		return this.cleanTexture.orElse(this.agedTexture.orElseThrow());
	}

	private static DataResult<Signage> validate(Signage signage) {
		if (signage.cleanTexture.isEmpty() && signage.agedTexture.isEmpty())
			return DataResult.error(() -> "Signage must have at least one texture present");
		return DataResult.success(signage);
	}
}
