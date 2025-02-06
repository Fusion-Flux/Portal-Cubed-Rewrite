package io.github.fusionflux.portalcubed.framework.key;

import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.DropPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.GrabPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class GrabKeyMappingAction implements KeyMappingAction {
	@Override
	public void onPressed(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (player == null || player.isSpectator() || player.pc$disintegrating())
			return;

		HoldableEntity held = player.getHeldEntity();
		if (held == null) { // not holding, grab
			HitResult hit = ProjectileUtil.getHitResultOnViewVector(
					player,
					EntitySelector.NO_SPECTATORS.and(Entity::isPickable),
					player.entityInteractionRange()
			);

			if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof HoldableEntity holdable) {
				PortalCubedPackets.sendToServer(new GrabPacket(holdable));
			} else {
				player.grabSoundManager().onFailedGrab();
			}
		} else { // currently holding, drop
			PortalCubedPackets.sendToServer(DropPacket.INSTANCE);
		}
	}
}
