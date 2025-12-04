package io.github.fusionflux.portalcubed_gametests;

import java.util.Optional;

import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.content.portal.color.ConstantPortalColor;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunShootContext;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;

public record PortalHelper(GameTestHelper helper, String key, SinglePortalHelper primary, SinglePortalHelper secondary) {
	public static final double POSITION_ASSERTION_EPSILON = 0.1;

	public PortalHelper(GameTestHelper helper, String key) {
		this(helper, key, Polarity.PRIMARY.defaultColor, Polarity.SECONDARY.defaultColor);
	}

	public PortalHelper(GameTestHelper helper, String key, int primaryColor, int secondaryColor) {
		this(
				helper, key,
				new SinglePortalHelper(
						helper, key,
						new PortalSettings(PortalType.ROUND, true, new ConstantPortalColor(primaryColor), false),
						Polarity.PRIMARY
				),
				new SinglePortalHelper(
						helper, key,
						new PortalSettings(PortalType.ROUND, true, new ConstantPortalColor(secondaryColor), false),
						Polarity.SECONDARY
				)
		);
	}

	public record SinglePortalHelper(GameTestHelper helper, String key, PortalSettings settings, Polarity polarity) {
		public void shootFrom(Vec3 pos, Direction facing) {
			this.shootFrom(pos, facing, 0);
		}

		public void shootFrom(Vec3 from, Direction facing, float yRot) {
			new PortalGunShootContext(this.key, this.helper.getLevel(), this.helper.absoluteVec(from), facing.getUnitVec3(), yRot)
					.shootAndPlace(Optional.empty(), this.polarity, this.settings);
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
			PortalInstance portal = this.getPortal().orElseThrow(() -> new GameTestAssertException("Expected " + this.polarity + " portal with key " + this.key + ", got nothing"));

			Vec3 origin = this.helper.relativeVec(portal.data.origin());
			if (Math.abs(origin.x - expectedX) > POSITION_ASSERTION_EPSILON || Math.abs(origin.y - expectedY) > POSITION_ASSERTION_EPSILON || Math.abs(origin.z - expectedZ) > POSITION_ASSERTION_EPSILON)
				throw new GameTestAssertException("Expected portal position to be " + new Vec3(expectedX, expectedY, expectedZ) + " got " + origin);

			Vector3d facing = portal.rotation().transformUnit(0, 1, 0, new Vector3d());
			Direction normal = Direction.getApproximateNearest(facing.x, facing.y, facing.z);
			if (normal != expectedNormal)
				throw new GameTestAssertException("Expected portal direction to be " + expectedNormal + ", got " + normal);
		}

		public void assertNotPresent() {
			if (this.getPortal().isPresent())
				throw new GameTestAssertException("Did not expect " + this.polarity + " portal with key " + this.key);
		}

		private Optional<PortalInstance> getPortal() {
			ServerPortalManager manager = this.helper.getLevel().portalManager();
			return manager.getOrEmpty(this.key).get(this.polarity);
		}
	}
}
