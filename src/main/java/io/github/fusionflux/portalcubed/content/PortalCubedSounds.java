package io.github.fusionflux.portalcubed.content;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

public class PortalCubedSounds {
	public static final SoundEvent FLOOR_BUTTON_PRESS = register("floor_button_press");
	public static final SoundEvent FLOOR_BUTTON_RELEASE = register("floor_button_release");
	public static final SoundEvent OLD_AP_FLOOR_BUTTON_PRESS = register("old_ap_floor_button_press");
	public static final SoundEvent OLD_AP_FLOOR_BUTTON_RELEASE = register("old_ap_floor_button_release");
	public static final SoundEvent PORTAL_1_FLOOR_BUTTON_PRESS = register("portal_1_floor_button_press");
	public static final SoundEvent PORTAL_1_FLOOR_BUTTON_RELEASE = register("portal_1_floor_button_release");

	public static final SoundEvent RADIO_SONG = register("radio");
	public static final SoundEvent COMPANION_CUBE_AMBIANCE = register("companion_cube_ambiance");

	public static final SoundEvent PORTAL_GUN_CANNOT_GRAB = register("portal_gun_cannot_grab");
	public static final SoundEvent PORTAL_GUN_GRAB = register("portal_gun_grab");
	public static final SoundEvent PORTAL_GUN_HOLD_LOOP = register("portal_gun_hold_loop");
	public static final SoundEvent PORTAL_GUN_DROP = register("portal_gun_drop");

	public static SoundEvent register(String name) {
		var id = PortalCubed.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	@ClientOnly
	public static FollowingSoundInstance createPortalGunHoldLoop(Player player) {
		var sound = new FollowingSoundInstance(PORTAL_GUN_HOLD_LOOP, player.getSoundSource(), player);
		sound.setLooping(true);
		return sound;
	}

	public static void init() {
	}
}
