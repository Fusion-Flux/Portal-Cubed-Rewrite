package io.github.fusionflux.portalcubed_gametests;

import java.util.UUID;

import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.PortalShape;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectile;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public record PortalHelper(GameTestHelper helper, UUID id, SinglePortalHelper primary, SinglePortalHelper secondary) {
	public PortalHelper(GameTestHelper helper, String key) {
		this(helper, key, Polarity.PRIMARY.defaultColor, Polarity.SECONDARY.defaultColor);
	}

	public PortalHelper(GameTestHelper helper, String key, int primaryColor, int secondaryColor) {
		this(helper, makeId(helper, key), primaryColor, secondaryColor);
	}

	public PortalHelper(GameTestHelper helper, UUID id, int primaryColor, int secondaryColor) {
		this(
				helper, id,
				new SinglePortalHelper(helper, id, new PortalSettings(primaryColor, PortalShape.SQUARE), Polarity.PRIMARY),
				new SinglePortalHelper(helper, id, new PortalSettings(secondaryColor, PortalShape.SQUARE), Polarity.SECONDARY)
		);
	}

	private static UUID makeId(GameTestHelper helper, String key) {
		BlockPos origin = helper.relativePos(BlockPos.ZERO);
		return PortalManager.generateId(key + origin);
	}

	public record SinglePortalHelper(GameTestHelper helper, UUID id, PortalSettings settings, Polarity polarity) {
		public void shootFrom(Vec3 pos, Direction facing) {
			this.shootFrom(pos, facing, 0);
		}

		public void shootFrom(Vec3 from, Direction facing, float yRot) {
			ServerLevel level = this.helper.getLevel();
			PortalProjectile projectile = new PortalProjectile(level, this.settings, yRot, this.id, this.polarity);
			projectile.moveTo(this.helper.absoluteVec(from));
			Vec3i normal = facing.getNormal();
			Vec3 vel = Vec3.atLowerCornerOf(normal).scale(PortalProjectile.SPEED);
			projectile.setDeltaMovement(vel);
			level.addFreshEntity(projectile);
		}

		public void placeOn(BlockPos surface, Direction normal) {
			this.placeOn(surface, normal, 0);
		}

		public void placeOn(BlockPos surface, Direction normal, float yRot) {
			Quaternionf rotation = PortalProjectile.getPortalRotation(normal, yRot);
			// shift the portal so the bottom half is centered on the surface
			Vector3f baseOffset = new Vector3f(0, 0.5f, 0);
			Vector3f offset = rotation.transform(baseOffset);

			Vec3 pos = Vec3.atCenterOf(this.helper.absolutePos(surface))
					.add(normal.getStepX() / 2f, normal.getStepY() / 2f, normal.getStepZ() / 2f)
					.add(offset.x, offset.y, offset.z);

			ServerPortalManager manager = this.helper.getLevel().portalManager();
			manager.createPortal(this.id, this.polarity, new PortalData(pos, rotation, this.settings));
		}
	}
}
