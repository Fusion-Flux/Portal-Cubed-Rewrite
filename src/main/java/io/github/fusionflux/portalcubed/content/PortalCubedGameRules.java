package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;

public class PortalCubedGameRules {
	public static final CustomGameRuleCategory CATEGORY = new CustomGameRuleCategory(
		PortalCubed.id(PortalCubed.ID),
		Component.translatable("gamerule.category.portalcubed").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)
	);

	public static final GameRules.Key<GameRules.BooleanValue> PROP_SNATCHING = GameRuleRegistry.register(
		"propSnatching", CATEGORY, GameRuleFactory.createBooleanRule(true)
	);

	public static final GameRules.Key<GameRules.BooleanValue> TOXIC_GOO_SOURCE_CONVERSION = GameRuleRegistry.register(
		"toxicGooSourceConversion", CATEGORY, GameRuleFactory.createBooleanRule(true)
	);

	public static final GameRules.Key<GameRules.IntegerValue> TOXIC_GOO_DAMAGE = GameRuleRegistry.register(
		"toxicGooDamage", CATEGORY, GameRuleFactory.createIntRule(10, 0, 1024)
	);

	public static final GameRules.Key<GameRules.IntegerValue> PORTAL_SHOT_RANGE_LIMIT = GameRuleRegistry.register(
			"portalShotRangeLimit", CATEGORY, GameRuleFactory.createIntRule(512, 1, 512)
	);

	public static final GameRules.Key<GameRules.BooleanValue> RESTRICT_VALID_PORTAL_SURFACES = GameRuleRegistry.register(
			"restrictValidPortalSurfaces", CATEGORY, GameRuleFactory.createBooleanRule(false)
	);

	// making this gamerule work with the current portal validation implementation is not possible.
	// it'll return at Some Point:tm: in the future.
	// public static final GameRules.Key<GameRules.BooleanValue> PORTALS_BUMP_THROUGH_WALLS = GameRuleRegistry.register(
	// 		"portalsBumpThroughWalls", CATEGORY, GameRuleFactory.createBooleanRule(true)
	// );

	public static final GameRules.Key<GameRules.BooleanValue> ALLOW_ROTATED_WALL_PORTALS = GameRuleRegistry.register(
			"allowRotatedWallPortals", CATEGORY, GameRuleFactory.createBooleanRule(false)
	);

	public static void init() {
	}
}
