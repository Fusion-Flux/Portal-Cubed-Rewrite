package io.github.fusionflux.portalcubed.content.goo;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;

// based on lava
public final class GooFogEnvironment extends FogEnvironment {
	public static final GooFogEnvironment INSTANCE = new GooFogEnvironment();
	public static final Vector3fc COLOR = new Vector3f(99, 29, 1).div(255);

	private GooFogEnvironment() {}

	@Override
	public void setupFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker) {
		Entity entity = camera.entity();

		if (entity != null && entity.isSpectator()) {
			fog.environmentalStart = -8;
			fog.environmentalEnd = renderDistance * 0.5f;
		} else {
			fog.environmentalStart = 0;
			fog.environmentalEnd = 3;
		}

		fog.skyEnd = fog.environmentalEnd;
		fog.cloudEnd = fog.environmentalEnd;
	}

	@Override
	public Vector3fc getBaseColor(ClientLevel level, Camera camera, int renderDistance, float partialTicks) {
		return COLOR;
	}

	@Override
	public boolean isApplicable(@Nullable FogType type, Entity entity) {
		return type == FogType.PORTALCUBED_GOO;
	}
}
