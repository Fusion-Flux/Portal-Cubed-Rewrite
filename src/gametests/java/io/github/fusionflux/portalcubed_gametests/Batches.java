package io.github.fusionflux.portalcubed_gametests;

import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;

public class Batches {
	public static final String RAINY = PortalCubedGameTests.ID + ":rainy";
	public static final String RESTRICTED_PORTAL_SURFACES = PortalCubedGameTests.ID + ":restricted_portal_surfaces";

	@BeforeBatch(batch = RAINY)
	public void startRain(ServerLevel level) {
		level.setWeatherParameters(0, 24000, true, false);
	}
	@AfterBatch(batch = RAINY)
	public void stopRain(ServerLevel level) {
		level.setWeatherParameters(24000, 0, false, false);
	}


	@BeforeBatch(batch = RESTRICTED_PORTAL_SURFACES)
	public void setPortalSurfaceRestrictions(ServerLevel level) {
		level.getGameRules().getRule(GameRules.RULE_LIMITED_CRAFTING).set(true, level.getServer()); //change rule to restrictValidPortalSurfaces once it exists
	}
	@AfterBatch(batch = RESTRICTED_PORTAL_SURFACES)
	public void removePortalSurfaceRestrictions(ServerLevel level) {
		level.getGameRules().getRule(GameRules.RULE_LIMITED_CRAFTING).set(false, level.getServer()); //change rule to restrictValidPortalSurfaces once it exists
	}

}
