package io.github.fusionflux.portalcubed.framework.particle;

import io.github.fusionflux.portalcubed.framework.render.PortalCubedRenderTypes;
import net.minecraft.client.particle.ParticleRenderType;

public interface PortalCubedParticleRenderTypes {
	ParticleRenderType MULTIPLY = new ParticleRenderType("portalcubed:multiply", PortalCubedRenderTypes.MULTIPLY_PARTICLE);
}
