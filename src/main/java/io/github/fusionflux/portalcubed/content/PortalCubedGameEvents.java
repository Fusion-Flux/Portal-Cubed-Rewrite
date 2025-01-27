package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.gameevent.GameEvent;

public class PortalCubedGameEvents {
	public static final Holder.Reference<GameEvent> CROWBAR_HIT = Registry.registerForHolder(
			BuiltInRegistries.GAME_EVENT,
			PortalCubed.id("crowbar_hit"),
			new GameEvent(16)
	);

	public static void init() {
	}
}
