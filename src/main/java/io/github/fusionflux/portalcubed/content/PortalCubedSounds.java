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

	public static final SoundEvent PORTAL_GUN_CANNOT_GRAB = register("portal_gun.cannot_grab");
	public static final SoundEvent PORTAL_GUN_GRAB = register("portal_gun.grab");
	public static final SoundEvent PORTAL_GUN_HOLD_LOOP = register("portal_gun.hold_loop");
	public static final SoundEvent PORTAL_GUN_DROP = register("portal_gun.drop");

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
