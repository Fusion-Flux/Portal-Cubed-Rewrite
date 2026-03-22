package io.github.fusionflux.portalcubed.framework.key;

import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.DropPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.GrabPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.ClipContext;

public class GrabKeyMappingAction implements KeyMappingAction {
	@Override
	public void onPressed(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (player == null || player.isSpectator() || player.pc$disintegrating())
			return;

		HoldableEntity held = player.getHeldEntity();
		if (held != null) {
			// currently holding, drop
			PortalCubedPackets.sendToServer(DropPacket.INSTANCE);
			return;
		}

		// not holding, grab
		RaycastOptions options = RaycastOptions.DEFAULT.edit()
				.blocks(ClipContext.Block.COLLIDER)
				.collisionContext(player)
				.build();

		RaycastResult result = options.raycast(player.level(), player.getEyePosition(), player.getLookAngle(), player.entityInteractionRange());

		if (result instanceof RaycastResult.Entity entityResult && entityResult.entity instanceof HoldableEntity holdable) {
			PortalCubedPackets.sendToServer(new GrabPacket(holdable));
		} else {
			player.grabSoundManager().onFailedGrab();
		}
	}
}
