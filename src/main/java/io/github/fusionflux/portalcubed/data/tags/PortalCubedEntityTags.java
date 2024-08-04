package io.github.fusionflux.portalcubed.data.tags;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class PortalCubedEntityTags {
	// props
	public static final TagKey<EntityType<?>> CAN_BE_DIRTY = create("can_be_dirty");
	public static final TagKey<EntityType<?>> CAN_BE_CHARRED = create("can_be_charred");
	public static final TagKey<EntityType<?>> CAN_BE_WASHED = create("can_be_washed");
	public static final TagKey<EntityType<?>> DEALS_LANDING_DAMAGE = create("deals_landing_damage");

	// floor buttons
	public static final TagKey<EntityType<?>> PRESSES_CUBE_BUTTONS = create("presses_cube_buttons");
	public static final TagKey<EntityType<?>> PRESSES_FLOOR_BUTTONS = create("presses_floor_buttons");
	public static final TagKey<EntityType<?>> PORTAL_BLACKLIST = create("portal_blacklist");

	public static final TagKey<EntityType<?>> IMMUNE_TO_TOXIC_GOO = create("immune_to_toxic_goo");

	// fizzling
	public static final TagKey<EntityType<?>> DISINTEGRATES_WHEN_FIZZLED = create("disintegrates_when_fizzled");
	public static final TagKey<EntityType<?>> IMMUNE_TO_DISINTEGRATION = create("immune_to_disintegration");
	public static final TagKey<EntityType<?>> FIZZLES_WITHOUT_DARK_PARTICLES = create("fizzles_without_dark_particles");
	public static final TagKey<EntityType<?>> FIZZLES_WITHOUT_BRIGHT_PARTICLES = create("fizzles_without_bright_particles");
	public static final TagKey<EntityType<?>> FIZZLES_WITHOUT_FLASH = create("fizzles_without_flash");
	public static final TagKey<EntityType<?>> FIZZLES_WITH_ALTERNATE_BRIGHT_PARTICLES = create("fizzles_with_alternate_bright_particles");

    private static TagKey<EntityType<?>> create(String name) {
		return TagKey.create(Registries.ENTITY_TYPE, PortalCubed.id(name));
	}
}
