package io.github.fusionflux.portalcubed.framework.render.debug;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class CameraRotator {
	private static double xRot, yRot;

	public static void tick(Minecraft mc) {
		if (!isActive()) {
			xRot = 0;
			yRot = 0;
		}
	}

	public static boolean handle(double yRot, double xRot) {
		if (isActive()) {
			// 0.15 is magic from Entity.turn
			CameraRotator.xRot += (xRot * 0.15);
			CameraRotator.yRot += (yRot * 0.15);
			return true;
		}

		return false;
	}

	public static double xRot() {
		return xRot;
	}

	public static double yRot() {
		return yRot;
	}

	public static boolean isActive() {
		return FabricLoader.getInstance().isDevelopmentEnvironment()
				&& InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_R);
	}
}
