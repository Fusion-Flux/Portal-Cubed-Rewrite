package io.github.fusionflux.portalcubed.content;

import java.util.EnumMap;
import java.util.Map;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.prop.ImpactSoundType;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;

public class PortalCubedSounds {
	public static final SoundEvent CHAMBER_DOOR_OPEN = register("chamber_door.portal_2.open");
	public static final SoundEvent CHAMBER_DOOR_UNLOCK = register("chamber_door.portal_2.unlock");
	public static final SoundEvent CHAMBER_DOOR_CLOSE = register("chamber_door.portal_2.close");
	public static final SoundEvent CHAMBER_DOOR_LOCK = register("chamber_door.portal_2.lock");
	public static final SoundEvent PEDESTAL_BUTTON_PRESS = register("pedestal_button.portal_2.press");
	public static final SoundEvent PEDESTAL_BUTTON_RELEASE = register("pedestal_button.portal_2.release");
	public static final SoundEvent FLOOR_BUTTON_PRESS = register("floor_button.portal_2.press");
	public static final SoundEvent FLOOR_BUTTON_RELEASE = register("floor_button.portal_2.release");
	public static final SoundEvent OLD_AP_CHAMBER_DOOR_OPEN = register("chamber_door.old_ap.open");
	public static final SoundEvent OLD_AP_CHAMBER_DOOR_CLOSE = register("chamber_door.old_ap.close");
	public static final SoundEvent OLD_AP_PEDESTAL_BUTTON_PRESS = register("pedestal_button.old_ap.press");
	public static final SoundEvent OLD_AP_PEDESTAL_BUTTON_RELEASE = register("pedestal_button.old_ap.release");
	public static final SoundEvent OLD_AP_FLOOR_BUTTON_PRESS = register("floor_button.old_ap.press");
	public static final SoundEvent OLD_AP_FLOOR_BUTTON_RELEASE = register("floor_button.old_ap.release");
	public static final SoundEvent PORTAL_1_CHAMBER_DOOR_OPEN = register("chamber_door.portal_1.open");
	public static final SoundEvent PORTAL_1_CHAMBER_DOOR_CLOSE = register("chamber_door.portal_1.close");
	public static final SoundEvent PORTAL_1_FLOOR_BUTTON_PRESS = register("floor_button.portal_1.press");
	public static final SoundEvent PORTAL_1_FLOOR_BUTTON_RELEASE = register("floor_button.portal_1.release");

	public static final SoundEvent OLD_AP_TIMER = register("old_ap_timer");
	public static final SoundEvent TIMER_DING = register("timer_ding");

	public static final SoundEvent RADIO_SONG = register("prop.radio.ambient");
	public static final SoundEvent COMPANION_CUBE_AMBIANCE = register("prop.companion_cube.ambient");

	public static final SoundEvent DEFAULT_PORTAL_GUN_PRIMARY_SHOOT = register("portal_gun.default.primary_shoot");
	public static final SoundEvent DEFAULT_PORTAL_GUN_SECONDARY_SHOOT = register("portal_gun.default.secondary_shoot");
	public static final SoundEvent DEFAULT_PORTAL_GUN_FIZZLE = register("portal_gun.default.fizzle");
	public static final SoundEvent DEFAULT_PORTAL_GUN_CANNOT_GRAB = register("portal_gun.default.cannot_grab");
	public static final SoundEvent DEFAULT_PORTAL_GUN_GRAB = register("portal_gun.default.grab");
	public static final SoundEvent DEFAULT_PORTAL_GUN_HOLD_LOOP = register("portal_gun.default.hold_loop");
	public static final SoundEvent DEFAULT_PORTAL_GUN_RELEASE = register("portal_gun.default.release");

	public static final SoundEvent PORTAL_1_PORTAL_GUN_PRIMARY_SHOOT = register("portal_gun.portal_1.primary_shoot");
	public static final SoundEvent PORTAL_1_PORTAL_GUN_SECONDARY_SHOOT = register("portal_gun.portal_1.secondary_shoot");
	public static final SoundEvent PORTAL_1_PORTAL_GUN_FIZZLE = register("portal_gun.portal_1.fizzle");
	public static final SoundEvent PORTAL_1_PORTAL_GUN_CANNOT_GRAB = register("portal_gun.portal_1.cannot_grab");
	public static final SoundEvent PORTAL_1_PORTAL_GUN_GRAB = register("portal_gun.portal_1.grab");
	public static final SoundEvent PORTAL_1_PORTAL_GUN_HOLD_LOOP = register("portal_gun.portal_1.hold_loop");

	public static final SoundEvent WAND_PORTAL_GUN_SHOOT = register("portal_gun.wand.shoot");
	public static final SoundEvent WAND_PORTAL_GUN_GRAB = register("portal_gun.wand.grab");
	public static final SoundEvent WAND_PORTAL_GUN_FIZZLE = register("portal_gun.wand.fizzle");

	public static final SoundEvent PAINTBRUSH_PORTAL_GUN_PRIMARY_SHOOT = register("portal_gun.paintbrush.primary_shoot");
	public static final SoundEvent PAINTBRUSH_PORTAL_GUN_SECONDARY_SHOOT = register("portal_gun.paintbrush.secondary_shoot");
	public static final SoundEvent PAINTBRUSH_PORTAL_GUN_FIZZLE = register("portal_gun.paintbrush.fizzle");

	public static final SoundEvent PISTOL_PORTAL_GUN_PRIMARY_SHOOT = register("portal_gun.pistol.primary_shoot");
	public static final SoundEvent PISTOL_PORTAL_GUN_SECONDARY_SHOOT = register("portal_gun.pistol.secondary_shoot");
	public static final SoundEvent PISTOL_PORTAL_GUN_FIZZLE = register("portal_gun.pistol.fizzle");

	public static final SoundEvent SPLASH_O_MATIC_PORTAL_GUN_SHOOT = register("portal_gun.splash_o_matic.shoot");
	public static final SoundEvent SPLASH_O_MATIC_PORTAL_GUN_FIZZLE = register("portal_gun.splash_o_matic.fizzle");

	public static final SoundEvent POLAROID_PORTAL_GUN_SHOOT = register("portal_gun.polaroid.shoot");
	public static final SoundEvent POLAROID_PORTAL_GUN_FIZZLE = register("portal_gun.polaroid.fizzle");

	public static final SoundEvent PIPIS_CANNON_PORTAL_GUN_SHOOT = register("portal_gun.pipis_cannon.shoot");
	public static final SoundEvent PIPIS_CANNON_PORTAL_GUN_FIZZLE = register("portal_gun.pipis_cannon.fizzle");

	public static final SoundEvent PEASHOOTER_PORTAL_GUN_SHOOT = register("portal_gun.peashooter.shoot");
	public static final SoundEvent PEASHOOTER_PORTAL_GUN_FIZZLE = register("portal_gun.peashooter.fizzle");

	public static final SoundEvent DEFAULT_PORTAL_OPEN_PRIMARY = register("portal_type.default.open.primary");
	public static final SoundEvent DEFAULT_PORTAL_OPEN_SECONDARY = register("portal_type.default.open.secondary");
	public static final SoundEvent DEFAULT_PORTAL_CLOSE_PRIMARY = register("portal_type.default.close.primary");
	public static final SoundEvent DEFAULT_PORTAL_CLOSE_SECONDARY = register("portal_type.default.close.secondary");
	public static final SoundEvent DEFAULT_PORTAL_CANT_OPEN = register("portal_type.default.cant_open");
	public static final SoundEvent DEFAULT_PORTAL_TRAVEL = register("portal_type.default.travel");
	public static final SoundEvent DEFAULT_PORTAL_AMBIENT = register("portal_type.default.ambient");

	public static final SoundEvent PORTAL_1_PORTAL_OPEN = register("portal_type.portal_1.open");
	public static final SoundEvent PORTAL_1_PORTAL_CLOSE = register("portal_type.portal_1.close");
	public static final SoundEvent PORTAL_1_PORTAL_CANT_OPEN = register("portal_type.portal_1.cant_open");
	public static final SoundEvent PORTAL_1_PORTAL_TRAVEL = register("portal_type.portal_1.travel");
	public static final SoundEvent PORTAL_1_PORTAL_AMBIENT = register("portal_type.portal_1.ambient");

	public static final SoundEvent PORTAL_COMBAT_PORTAL = register("portal_type.portal_combat");

	public static final SoundEvent CONSTRUCTION_CANNON_OBSTRUCTED = register("construction_cannon.obstructed");
	public static final SoundEvent CONSTRUCTION_CANNON_MISSING_MATERIALS = register("construction_cannon.missing_materials");

	public static final SoundEvent CROWBAR_SWING = register("crowbar.swing");

	public static final SoundEvent CONCRETE_SURFACE_IMPACT = register("surface_impact.concrete");
	public static final SoundEvent GLASS_SURFACE_IMPACT = register("surface_impact.glass");
	public static final SoundEvent METAL_SURFACE_IMPACT = register("surface_impact.metal");

	public static final Map<ImpactSoundType, SoundEvent> IMPACTS = Util.make(new EnumMap<>(ImpactSoundType.class), map -> {
		for (ImpactSoundType type : ImpactSoundType.values()) {
			map.put(type, register("prop." + type.toString() + ".impact"));
		}
	});
	public static final SoundEvent FIDDLE_STICKS = register("prop.error.impact");
	public static final SoundEvent GENERIC_DISINTEGRATION = register("prop.generic.disintegration");
	public static final SoundEvent RADIO_DISINTEGRATION = register("prop.radio.disintegration");

	public static final SoundEvent SURPRISE = register("surprise");

	public static final SoundEvent SEWAGE_STEP = register("block.sewage.step");

	public static SoundEvent register(String name) {
		ResourceLocation id = PortalCubed.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static SoundEvent timerDing(RandomSource random) {
		return random.nextInt(10) >= random.nextInt(100) ? SURPRISE : TIMER_DING;
	}

	public static void init() {
	}
}
