package io.github.fusionflux.portalcubed_gametests;

import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.server.level.ServerLevel;

public class Batches {
	public static final String RAINY = PortalCubedGameTests.ID + ":rainy";
	public static final String RESTRICTED_PORTAL_SURFACES = PortalCubedGameTests.ID + ":restricted_portal_surfaces";
	// public static final String PORTALS_BUMP_THROUGH_WALLS = PortalCubedGameTests.ID + ":portals_bump_through_walls";
	public static final String PORTALS_DO_NOT_BUMP_THROUGH_WALLS = PortalCubedGameTests.ID + ":portals_do_not_bump_through_walls";

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
		level.getGameRules().getRule(PortalCubedGameRules.RESTRICT_VALID_PORTAL_SURFACES).set(true, level.getServer());
	}
	@AfterBatch(batch = RESTRICTED_PORTAL_SURFACES)
	public void removePortalSurfaceRestrictions(ServerLevel level) {
		level.getGameRules().getRule(PortalCubedGameRules.RESTRICT_VALID_PORTAL_SURFACES).set(false, level.getServer());
	}

	// @BeforeBatch(batch = PORTALS_BUMP_THROUGH_WALLS)
	// public void resetPortalBumpingGamerule(ServerLevel level) {
	// 	level.getGameRules().getRule(PortalCubedGameRules.PORTALS_BUMP_THROUGH_WALLS).set(true, level.getServer());
	// }
	//
	// @BeforeBatch(batch = PORTALS_DO_NOT_BUMP_THROUGH_WALLS)
	// public void disablePortalBumpingGamerule(ServerLevel level) {
	// 	level.getGameRules().getRule(PortalCubedGameRules.PORTALS_BUMP_THROUGH_WALLS).set(false, level.getServer());
	// }
	// @AfterBatch(batch = PORTALS_DO_NOT_BUMP_THROUGH_WALLS)
	// public void enablePortalBumpingGamerule(ServerLevel level) {
	// 	level.getGameRules().getRule(PortalCubedGameRules.PORTALS_BUMP_THROUGH_WALLS).set(true, level.getServer());
	// }

}
