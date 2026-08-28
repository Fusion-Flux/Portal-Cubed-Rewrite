package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

// TODO: All of these. Make helper methods? - Max
public class PortalCubedGameRules {
	public static final GameRuleCategory CATEGORY = GameRuleCategory.register(PortalCubed.id(PortalCubed.ID));

	public static final GameRule<Boolean> PROP_SNATCHING = GameRuleBuilder.forBoolean(true)
			.category(CATEGORY)
			.buildAndRegister(PortalCubed.id("propSnatching"));

	public static final GameRule<Boolean> TOXIC_GOO_SOURCE_CONVERSION = GameRuleRegistry.register(
		"toxicGooSourceConversion", CATEGORY, GameRuleFactory.createBooleanRule(true)
	);

	public static final GameRule<Integer> TOXIC_GOO_DAMAGE = GameRuleRegistry.register(
		"toxicGooDamage", CATEGORY, GameRuleFactory.createIntRule(10, 0, 1024)
	);

	public static final GameRule<Integer> PORTAL_SHOT_RANGE_LIMIT = GameRuleRegistry.register(
			"portalShotRangeLimit", CATEGORY, GameRuleFactory.createIntRule(512, 1, 512)
	);

	public static final GameRule<Boolean> RESTRICT_VALID_PORTAL_SURFACES = GameRuleRegistry.register(
			"restrictValidPortalSurfaces", CATEGORY, GameRuleFactory.createBooleanRule(false)
	);

	public static final GameRule<Boolean> PORTALS_BUMP_THROUGH_WALLS = GameRuleRegistry.register(
			"portalsBumpThroughWalls", CATEGORY, GameRuleFactory.createBooleanRule(true)
	);

	public static final GameRule<Boolean> ALLOW_ROTATED_WALL_PORTALS = GameRuleRegistry.register(
			"allowRotatedWallPortals", CATEGORY, GameRuleFactory.createBooleanRule(false)
	);

	public static final GameRule<Boolean> MANUAL_PORTAL_CLEARING = GameRuleRegistry.register(
			"manualPortalClearing", CATEGORY, GameRuleFactory.createBooleanRule(true)
	);

	public static final GameRule<Boolean> ALLOW_SHOOTING_PORTALS_THROUGH_PORTALS = GameRuleRegistry.register(
			"allowShootingPortalsThroughPortals", CATEGORY, GameRuleFactory.createBooleanRule(false)
	);

	public static void init() {
	}
}
