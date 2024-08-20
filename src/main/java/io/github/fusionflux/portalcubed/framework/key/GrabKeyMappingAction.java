package io.github.fusionflux.portalcubed.framework.key;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.DropPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.GrabPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class GrabKeyMappingAction implements KeyMappingAction {
	@Override
	public void onPressed(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (player == null || player.isSpectator() || player.pc$disintegrating())
			return;

		HoldableEntity held = player.getHeldEntity();
		if (held == null) { // not holding, grab
			Vec3 lookVec = player.getLookAngle().scale(3);
			AABB checkBox = player.getBoundingBox().expandTowards(lookVec).inflate(1);

			var startPos = player.getEyePosition();
			var endPos = startPos.add(lookVec);

			HitResult hit = ProjectileUtil.getHitResultOnViewVector(
					player,
					EntitySelector.NO_SPECTATORS.and(Entity::isPickable),
					3
			);

			if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof HoldableEntity holdable) {
				PortalCubedPackets.sendToServer(new GrabPacket(holdable));
			} else if (player.isHolding(PortalCubedItems.PORTAL_GUN)) { // failed, play fail sound if holding portal gun
				player.playSound(PortalCubedSounds.PORTAL_GUN_CANNOT_GRAB);
			}
		} else { // currently holding, drop
			PortalCubedPackets.sendToServer(new DropPacket());
		}
	}
}
