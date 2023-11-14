package io.github.fusionflux.portalcubed_test;

import io.github.fusionflux.portalcubed.framework.particle.DecalParticle;
import io.github.fusionflux.portalcubed.framework.particle.ParaboloidParticle;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class PortalCubedTestmodClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		ParticleFactoryRegistry.getInstance().register(PortalCubedTestmod.TEST_PARTICLE, DecalParticle.Provider::new);
	}
}
