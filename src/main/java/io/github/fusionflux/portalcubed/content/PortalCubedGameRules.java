package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.registration.gamerule.CategoryHelper;
import net.minecraft.world.level.gamerules.GameRule;

public class PortalCubedGameRules {
	public static final CategoryHelper MISC = PortalCubed.REGISTRAR.gameRules.createCategory("misc");

	public static final GameRule<Boolean> PROP_SNATCHING = MISC.createBool("prop_snatching", true);
	public static final GameRule<Boolean> TOXIC_GOO_SOURCE_CONVERSION = MISC.createBool("toxic_goo_source_conversion", true);
	public static final GameRule<Integer> TOXIC_GOO_DAMAGE = MISC.createInt("toxic_goo_damage", 10, 0, 1024);

	public static final CategoryHelper PORTALS = PortalCubed.REGISTRAR.gameRules.createCategory("portals");

	public static final GameRule<Integer> PORTAL_SHOT_RANGE_LIMIT = PORTALS.createInt("portal_shot_range_limit", 512, 1, 512);
	public static final GameRule<Boolean> RESTRICT_VALID_PORTAL_SURFACES = PORTALS.createBool("restrict_valid_portal_surfaces", false);
	public static final GameRule<Boolean> PORTALS_BUMP_THROUGH_WALLS = PORTALS.createBool("portals_bump_through_walls", true);
	public static final GameRule<Boolean> ALLOW_ROTATED_WALL_PORTALS = PORTALS.createBool("allow_rotated_wall_portals", false);
	public static final GameRule<Boolean> MANUAL_PORTAL_CLEARING = PORTALS.createBool("manual_portal_clearing", true);
	public static final GameRule<Boolean> ALLOW_SHOOTING_PORTALS_THROUGH_PORTALS = PORTALS.createBool("allow_shooting_portals_through_portals", false);

	public static void init() {
	}
}
