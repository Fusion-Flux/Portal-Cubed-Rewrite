package io.github.fusionflux.portalcubed.content.misc;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import io.github.fusionflux.portalcubed.framework.registration.ParticleRenderTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public enum BulletHoleMaterial {
	CONCRETE(PortalCubedBlockTags.BULLET_HOLE_CONCRETE, PortalCubedSounds.CONCRETE_SURFACE_IMPACT, ParticleRenderTypes.MULTIPLY, false),
	GLASS(PortalCubedBlockTags.BULLET_HOLE_GLASS, PortalCubedSounds.GLASS_SURFACE_IMPACT, ParticleRenderTypes.TRANSLUCENT, true),
	METAL(PortalCubedBlockTags.BULLET_HOLE_METAL, PortalCubedSounds.METAL_SURFACE_IMPACT, ParticleRenderTypes.MULTIPLY, false);

	public final TagKey<Block> tag;
	public final SoundEvent impactSound;
	public final ParticleRenderTypes particleRenderType;
	public final boolean randomParticleRotation;

	BulletHoleMaterial(TagKey<Block> tag, SoundEvent impactSound, ParticleRenderTypes particleRenderType, boolean randomParticleRotation) {
		this.tag = tag;
		this.impactSound = impactSound;
		this.particleRenderType = particleRenderType;
		this.randomParticleRotation = randomParticleRotation;
	}

	public static Optional<BulletHoleMaterial> forState(BlockState state) {
		for (BulletHoleMaterial material : values()) {
			if (state.is(material.tag)) return Optional.of(material);
		}
		return Optional.empty();
	}
}
