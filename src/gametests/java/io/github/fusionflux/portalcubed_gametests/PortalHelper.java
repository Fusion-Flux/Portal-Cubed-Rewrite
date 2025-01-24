package io.github.fusionflux.portalcubed_gametests;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.PortalShape;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public record PortalHelper(GameTestHelper helper, String key, SinglePortalHelper primary, SinglePortalHelper secondary) {
	public PortalHelper(GameTestHelper helper, String key) {
		this(helper, key, Polarity.PRIMARY.defaultColor, Polarity.SECONDARY.defaultColor);
	}

	public PortalHelper(GameTestHelper helper, String key, int primaryColor, int secondaryColor) {
		this(
				helper, key,
				new SinglePortalHelper(
						helper, key,
						new PortalSettings(primaryColor, PortalShape.SQUARE, false, true),
						Polarity.PRIMARY
				),
				new SinglePortalHelper(
						helper, key,
						new PortalSettings(secondaryColor, PortalShape.SQUARE, false, true),
						Polarity.SECONDARY
				)
		);
	}

	public record SinglePortalHelper(GameTestHelper helper, String key, PortalSettings settings, Polarity polarity) {
		public void shootFrom(Vec3 pos, Direction facing) {
			this.shootFrom(pos, facing, 0);
		}

		public void shootFrom(Vec3 from, Direction facing, float yRot) {
			ServerLevel level = this.helper.getLevel();
			PortalProjectile projectile = new PortalProjectile(level, this.settings, yRot, this.key, this.polarity);
			projectile.moveTo(this.helper.absoluteVec(from));
			Vec3 vel = facing.getUnitVec3().scale(PortalProjectile.SPEED);
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
			manager.createPortal(this.key, this.polarity, new PortalData(pos, rotation, this.settings));
		}
	}
}
