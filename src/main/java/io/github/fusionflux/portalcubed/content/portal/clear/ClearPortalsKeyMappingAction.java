package io.github.fusionflux.portalcubed.content.portal.clear;

import io.github.fusionflux.portalcubed.framework.key.KeyMappingAction;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class ClearPortalsKeyMappingAction implements KeyMappingAction {
	@Override
	public void onPressed(Minecraft mc) {
		ClientPlayNetworking.send(ClearPortalsPacket.INSTANCE);
	}
}
