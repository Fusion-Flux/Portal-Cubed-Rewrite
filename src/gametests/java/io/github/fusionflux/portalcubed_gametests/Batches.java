package io.github.fusionflux.portalcubed_gametests;

import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.server.level.ServerLevel;

public class Batches {
	public static final String RAINY = PortalCubedGameTests.ID + ":rainy";

	@BeforeBatch(batch = RAINY)
	public void startRain(ServerLevel level) {
		level.setWeatherParameters(0, 24000, true, false);
	}

	@AfterBatch(batch = RAINY)
	public void stopRain(ServerLevel level) {
		level.setWeatherParameters(24000, 0, false, false);
	}
}
