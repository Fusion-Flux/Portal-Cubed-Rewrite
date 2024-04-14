package io.github.fusionflux.portalcubed.framework.key;

import java.util.OptionalInt;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
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
import net.minecraft.world.phys.Vec3;

public class GrabKeyMappingAction implements KeyMappingAction {
	@Override
	public void onPressed(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (player == null || player.isSpectator())
			return;

		OptionalInt heldPropId = ((PlayerExt) player).pc$getHeldProp();
		if (heldPropId.isEmpty()) { // not holding, grab
			Vec3 lookVec = player.getLookAngle().scale(3);
			AABB checkBox = player.getBoundingBox().expandTowards(lookVec).inflate(1);

			var startPos = player.getEyePosition();
			var endPos = startPos.add(lookVec);

			EntityHitResult hit = ProjectileUtil.getEntityHitResult(
					player, startPos, endPos, checkBox,
					EntitySelector.NO_SPECTATORS.and(Entity::isPickable),
					HoldableEntity.MAX_DIST_SQR
			);

			if (hit != null && hit.getEntity() instanceof HoldableEntity holdable) {
				PortalCubedPackets.sendToServer(new GrabPacket(holdable));
			} else if (player.isHolding(PortalCubedItems.PORTAL_GUN)) { // failed, play fail sound if holding portal gun
				player.playSound(PortalCubedSounds.PORTAL_GUN_CANNOT_GRAB);
			}
		} else { // currently holding, drop
			PortalCubedPackets.sendToServer(new DropPacket());
		}
	}
}
