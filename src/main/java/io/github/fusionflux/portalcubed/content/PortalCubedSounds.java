package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public class PortalCubedSounds {
	public static final SoundEvent FLOOR_BUTTON_PRESS = register("floor_button_press");
	public static final SoundEvent FLOOR_BUTTON_RELEASE = register("floor_button_release");
	public static final SoundEvent OLD_AP_FLOOR_BUTTON_PRESS = register("old_ap_floor_button_press");
	public static final SoundEvent OLD_AP_FLOOR_BUTTON_RELEASE = register("old_ap_floor_button_release");
	public static final SoundEvent PORTAL_1_FLOOR_BUTTON_PRESS = register("portal_1_floor_button_press");
	public static final SoundEvent PORTAL_1_FLOOR_BUTTON_RELEASE = register("portal_1_floor_button_release");

	public static final SoundEvent RADIO_SONG = register("radio");
	public static final SoundEvent COMPANION_CUBE_AMBIANCE = register("companion_cube_ambiance");

	public static SoundEvent register(String name) {
		var id = PortalCubed.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void init() {
	}
}
