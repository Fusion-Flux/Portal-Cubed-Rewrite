package io.github.fusionflux.portalcubed_gametests;

import java.util.Optional;

import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import io.github.fusionflux.portalcubed.content.portal.graphics.color.ConstantPortalColor;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunShootContext;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class PortalHelper {
	public static final double POSITION_ASSERTION_EPSILON = 0.1;

	public final SinglePortalHelper primary;
	public final SinglePortalHelper secondary;

	private final GameTestHelper helper;
	private final String key;

	public PortalHelper(GameTestHelper helper, String key) {
		this(helper, key, Polarity.PRIMARY.defaultColor, Polarity.SECONDARY.defaultColor);
	}

	public PortalHelper(GameTestHelper helper, String key, int primaryColor, int secondaryColor) {
		this.helper = helper;
		this.key = this.uniquifyKey(key);
		this.primary = new SinglePortalHelper(defaultSettings(primaryColor), Polarity.PRIMARY);
		this.secondary = new SinglePortalHelper(defaultSettings(secondaryColor), Polarity.SECONDARY);
	}

	private String uniquifyKey(String key) {
		BlockPos origin = this.helper.absolutePos(BlockPos.ZERO);
		return key + '(' + origin.getX() + ',' + origin.getY() + ',' + origin.getZ() + ')';
	}

	private static PortalSettings defaultSettings(int color) {
		return new PortalSettings(PortalType.ROUND, true, new ConstantPortalColor(color), false);
	}

	public final class SinglePortalHelper {
		private final PortalSettings settings;
		private final Polarity polarity;

		public SinglePortalHelper(PortalSettings settings, Polarity polarity) {
			this.settings = settings;
			this.polarity = polarity;
		}

		public void shootFrom(double x, double y, double z, Direction facing, float yRot) {
			this.shootFrom(new Vec3(x, y, z), facing, yRot);
		}

		public void shootFrom(Vec3 pos, Direction facing) {
			this.shootFrom(pos, facing, 0);
		}

		public void shootFrom(Vec3 from, Direction facing, float yRot) {
			ServerLevel level = PortalHelper.this.helper.getLevel();
			Vec3 pos = PortalHelper.this.helper.absoluteVec(from);
			Vec3 direction = facing.getUnitVec3();

			new PortalGunShootContext(PortalHelper.this.key, level, pos, direction, yRot).shootAndPlace(
					Optional.empty(), this.polarity, this.settings
			);
		}

		public void placeOn(int x, int y, int z, Direction normal) {
			this.placeOn(new BlockPos(x, y, z), normal);
		}

		public void placeOn(int x, int y, int z, Direction normal, float yRot) {
			this.placeOn(new BlockPos(x, y, z), normal, yRot);
		}

		public void placeOn(BlockPos surface, Direction normal) {
			this.placeOn(surface, normal, 0);
		}

		public void placeOn(BlockPos surface, Direction normal, float yRot) {
			// we want the bottom of the portal to be centered on the surface, so we need to shoot from 0.5 blocks "up"
			Quaternionf rotation = PortalData.normalToRotation(normal, yRot);
			Vector3f offsetUp = rotation.transformUnit(new Vector3f(0, 0, 0.5f));

			Vec3 offsetFromWall = normal.getUnitVec3().scale(0.75);

			Vec3 from = Vec3.atCenterOf(surface).add(offsetFromWall).add(offsetUp.x, offsetUp.y, offsetUp.z);
			this.shootFrom(from, normal.getOpposite(), yRot);
		}

		public void assertPresent(double expectedX, double expectedY, double expectedZ, Direction expectedNormal) {
			Portal portal = this.getPortal().orElseThrow(
					() -> new GameTestAssertException("Expected " + this.polarity + " portal with key " + PortalHelper.this.key + ", got nothing")
			);

			Vec3 origin = PortalHelper.this.helper.relativeVec(portal.data.origin());
			Vec3 expectedVec = new Vec3(expectedX, expectedY, expectedZ);
			if (origin.distanceTo(expectedVec) > POSITION_ASSERTION_EPSILON) {
				throw new GameTestAssertException("Expected portal position to be " + expectedVec + " got " + origin);
			}

			Vector3d facing = portal.rotation().transformUnit(0, 1, 0, new Vector3d());
			Direction normal = Direction.getApproximateNearest(facing.x, facing.y, facing.z);
			if (normal != expectedNormal) {
				throw new GameTestAssertException("Expected portal direction to be " + expectedNormal + ", got " + normal);
			}
		}

		public void assertNotPresent() {
			if (this.getPortal().isPresent())
				throw new GameTestAssertException("Did not expect " + this.polarity + " portal with key " + PortalHelper.this.key);
		}

		private Optional<Portal> getPortal() {
			ServerPortalManager manager = PortalHelper.this.helper.getLevel().portalManager();
			return manager.getPairOrEmpty(PortalHelper.this.key).get(this.polarity);
		}
	}
}
