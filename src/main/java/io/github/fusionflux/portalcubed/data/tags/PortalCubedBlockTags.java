package io.github.fusionflux.portalcubed.data.tags;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class PortalCubedBlockTags {
	public static final TagKey<Block> MAGNESIUM_FIRE_BASE_BLOCKS = create("magnesium_fire_base_blocks");
	public static final TagKey<Block> LEMON_LOGS = create("lemon_logs");
	public static final TagKey<Block> CANNON_REPLACEABLE = create("construction_cannon_replaceable");
	public static final TagKey<Block> CONFETTI = create("confetti");
	public static final TagKey<Block> CONNECTING_DIRECTIONAL_BLOCKS = create("connecting_directional_blocks");

	public static final TagKey<Block> BULLET_HOLE_CONCRETE = create("bullet_hole_concrete");
	public static final TagKey<Block> BULLET_HOLE_GLASS = create("bullet_hole_glass");
	public static final TagKey<Block> BULLET_HOLE_METAL = create("bullet_hole_metal");
	public static final TagKey<Block> CROWBAR_MAKES_HOLES = create("crowbar_makes_holes");

	public static final TagKey<Block> NONSOLID_TO_PORTALS = create("nonsolid_to_portals");
	public static final TagKey<Block> NONSOLID_TO_PORTAL_SHOTS = create("nonsolid_to_portal_shots");
	public static final TagKey<Block> UNRESTRICTED_PORTAL_SURFACES = create("unrestricted_portal_surfaces");
	public static final TagKey<Block> CANT_PLACE_PORTAL_ON = create("cant_place_portal_on");
	public static final TagKey<Block> OVERRIDES_PORTALABILITY = create("overrides_portalability");

	public static final TagKey<Block> PORTALS_USE_BASE_SHAPE = create("portals_use_base_shape");

	private static TagKey<Block> create(String name) {
		return TagKey.create(Registries.BLOCK, PortalCubed.id(name));
	}

	private static TagKey<Block> createCommon(String name) {
		return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", name));
	}
}
