package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class PortalCubedGameEvents {
	// it's a fastutil map stored as a ToIntFunction
	private static final Reference2IntMap<ResourceKey<GameEvent>> frequencies = (Reference2IntMap<ResourceKey<GameEvent>>) VibrationSystem.VIBRATION_FREQUENCY_FOR_EVENT;

	public static final Holder.Reference<GameEvent>
			CROWBAR_HIT = register("crowbar_hit", GameEvent.ENTITY_DAMAGE),
			PORTAL_PLACE = register("portal_place", GameEvent.BLOCK_PLACE),
			PORTAL_SHOT_FAIL = register("portal_shot_fail", GameEvent.PROJECTILE_LAND),
			PORTAL_REMOVE = register("portal_remove", GameEvent.BLOCK_DESTROY),
			PORTAL_TELEPORT_ENTER = register("portal_teleport_enter", GameEvent.ENTITY_MOUNT),
			PORTAL_TELEPORT_EXIT = register("portal_teleport_exit", GameEvent.ENTITY_DISMOUNT);

	public static void init() {}

	private static int frequencyOf(Holder.Reference<GameEvent> event) {
		return VibrationSystem.getGameEventFrequency(event);
	}

	private static Holder.Reference<GameEvent> register(String name, Holder.Reference<GameEvent> frequencySource) {
		return register(name, 16, frequencyOf(frequencySource));
	}

	private static Holder.Reference<GameEvent> register(String name, int radius, int frequency) {
		ResourceLocation id = PortalCubed.id(name);
		GameEvent event = new GameEvent(radius);
		Holder.Reference<GameEvent> holder = Registry.registerForHolder(BuiltInRegistries.GAME_EVENT, id, event);
		frequencies.put(holder.key(), frequency);
		return holder;
	}
}
