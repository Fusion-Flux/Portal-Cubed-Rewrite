package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.content.prop.ImpactSoundType;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

import java.util.EnumMap;
import java.util.Map;

public class PortalCubedSounds {
	public static final SoundEvent PEDESTAL_BUTTON_PRESS = register("pedestal_button_press");
	public static final SoundEvent PEDESTAL_BUTTON_RELEASE = register("pedestal_button_release");
	public static final SoundEvent FLOOR_BUTTON_PRESS = register("floor_button_press");
	public static final SoundEvent FLOOR_BUTTON_RELEASE = register("floor_button_release");
	public static final SoundEvent OLD_AP_PEDESTAL_BUTTON_PRESS = register("old_ap_pedestal_button_press");
	public static final SoundEvent OLD_AP_PEDESTAL_BUTTON_RELEASE = register("old_ap_pedestal_button_release");
	public static final SoundEvent OLD_AP_FLOOR_BUTTON_PRESS = register("old_ap_floor_button_press");
	public static final SoundEvent OLD_AP_FLOOR_BUTTON_RELEASE = register("old_ap_floor_button_release");
	public static final SoundEvent PORTAL_1_FLOOR_BUTTON_PRESS = register("portal_1_floor_button_press");
	public static final SoundEvent PORTAL_1_FLOOR_BUTTON_RELEASE = register("portal_1_floor_button_release");

	public static final SoundEvent OLD_AP_TIMER = register("old_ap_timer");
	public static final SoundEvent TIMER_DING = register("timer_ding");

	public static final SoundEvent RADIO_SONG = register("radio");
	public static final SoundEvent COMPANION_CUBE_AMBIANCE = register("companion_cube_ambiance");

	public static final SoundEvent PORTAL_GUN_CANNOT_GRAB = register("portal_gun_cannot_grab");
	public static final SoundEvent PORTAL_GUN_GRAB = register("portal_gun_grab");
	public static final SoundEvent PORTAL_GUN_HOLD_LOOP = register("portal_gun_hold_loop");
	public static final SoundEvent PORTAL_GUN_DROP = register("portal_gun_drop");

	public static final SoundEvent CONSTRUCTION_CANNON_OBSTRUCTED = register("construction_cannon_obstructed");
	public static final SoundEvent CONSTRUCTION_CANNON_MISSING_MATERIALS = register("construction_cannon_missing_materials");

	public static final SoundEvent CROWBAR_SWING = register("crowbar_swing");

	public static final SoundEvent CONCRETE_SURFACE_IMPACT = register("concrete_surface_impact");
	public static final SoundEvent GLASS_SURFACE_IMPACT = register("glass_surface_impact");
	public static final SoundEvent METAL_SURFACE_IMPACT = register("metal_surface_impact");

	public static final Map<ImpactSoundType, SoundEvent> IMPACTS = Util.make(new EnumMap<>(ImpactSoundType.class), map -> {
		for (ImpactSoundType type : ImpactSoundType.values()) {
			map.put(type, register(type.toString() + "_impact"));
		}
	});

	public static final SoundEvent SURPRISE = register("surprise");
	public static final SoundEvent FIDDLE_STICKS = register("error_impact");

	public static final SoundEvent SEWAGE_STEP = register("sewage_step");

	public static SoundEvent register(String name) {
		var id = PortalCubed.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static SoundEvent timerDing(RandomSource random) {
		return random.nextInt(10) >= random.nextInt(100) ? SURPRISE : TIMER_DING;
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
