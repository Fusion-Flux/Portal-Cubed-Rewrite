package io.github.fusionflux.portalcubed.framework.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class ClientTicks {
	private static int ticks;

	@Environment(EnvType.CLIENT)
	public static void tick(Minecraft client) {
		if (!client.isPaused())
			ticks++;
	}

	@Environment(EnvType.CLIENT)
	public static float get() {
		return ticks + Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
	}

	/**
	 * Returns {@link #get()} on client and 0 on server, only use this if there is really no better solution.
	 */
	public static float tryGet() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			return get();
		} else {
			return 0;
		}
	}
}
