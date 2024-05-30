package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.framework.particle.DecalParticleLightCache;

public interface ParticleEngineExt {
	// No prefix needed, guaranteed unique by DecalParticleLightCache
	DecalParticleLightCache getDecalParticleLightCache();
}
