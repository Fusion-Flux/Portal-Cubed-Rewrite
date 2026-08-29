package io.github.fusionflux.portalcubed.content.misc;

import java.util.Optional;
import java.util.function.Supplier;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public enum BulletHoleMaterial {
	// TODO: Figure out how particle groups work - Max
	CONCRETE(PortalCubedBlockTags.BULLET_HOLE_CONCRETE, PortalCubedSounds.CONCRETE_SURFACE_IMPACT, () -> () -> SingleQuadParticle.Layer.TRANSLUCENT, false), // TODO: multiply - Max
	GLASS(PortalCubedBlockTags.BULLET_HOLE_GLASS, PortalCubedSounds.GLASS_SURFACE_IMPACT, () -> () -> SingleQuadParticle.Layer.TRANSLUCENT, true),
	METAL(PortalCubedBlockTags.BULLET_HOLE_METAL, PortalCubedSounds.METAL_SURFACE_IMPACT, () -> () -> SingleQuadParticle.Layer.TRANSLUCENT, false); // TODO: multiply - Max

	public final TagKey<Block> tag;
	public final SoundEvent impactSound;
	public final Supplier<Supplier<SingleQuadParticle.Layer>> particleLayerSupplier;
	public final boolean randomParticleRotation;

	BulletHoleMaterial(TagKey<Block> tag, SoundEvent impactSound, Supplier<Supplier<SingleQuadParticle.Layer>> particleLayerSupplier, boolean randomParticleRotation) {
		this.tag = tag;
		this.impactSound = impactSound;
		this.particleLayerSupplier = particleLayerSupplier;
		this.randomParticleRotation = randomParticleRotation;
	}

	public static Optional<BulletHoleMaterial> forState(BlockState state) {
		for (BulletHoleMaterial material : values()) {
			if (state.is(material.tag)) return Optional.of(material);
		}
		return Optional.empty();
	}
}
