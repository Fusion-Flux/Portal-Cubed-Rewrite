package io.github.fusionflux.portalcubed.content.misc;

import java.util.Optional;

import io.github.fusionflux.portalcubed.content.PortalCubedParticles;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public enum BulletHoleMaterial {
	// TODO: Figure out how particle groups work - Max
	CONCRETE(PortalCubedBlockTags.BULLET_HOLE_CONCRETE, PortalCubedSounds.CONCRETE_SURFACE_IMPACT, PortalCubedParticles.CONCRETE_BULLET_HOLE),
	GLASS(PortalCubedBlockTags.BULLET_HOLE_GLASS, PortalCubedSounds.GLASS_SURFACE_IMPACT, PortalCubedParticles.GLASS_BULLET_HOLE),
	METAL(PortalCubedBlockTags.BULLET_HOLE_METAL, PortalCubedSounds.METAL_SURFACE_IMPACT, PortalCubedParticles.METAL_BULLET_HOLE);

	public final TagKey<Block> tag;
	public final SoundEvent impactSound;
	public final ParticleOptions particleType;

	BulletHoleMaterial(TagKey<Block> tag, SoundEvent impactSound, ParticleOptions particleType) {
		this.tag = tag;
		this.impactSound = impactSound;
		this.particleType = particleType;
	}

	public static Optional<BulletHoleMaterial> forState(BlockState state) {
		for (BulletHoleMaterial material : values()) {
			if (state.is(material.tag)) return Optional.of(material);
		}
		return Optional.empty();
	}
}
