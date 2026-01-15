package io.github.fusionflux.portalcubed_gametests;

import java.util.Optional;

import org.joml.Vector3d;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.clip.PortalShot;
import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import io.github.fusionflux.portalcubed.content.portal.graphics.color.ConstantPortalColor;
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
		return new PortalSettings(PortalType.ROUND, true, new ConstantPortalColor(color), false, true);
	}

	public final class SinglePortalHelper {
		private final PortalSettings settings;
		private final PortalId id;

		public SinglePortalHelper(PortalSettings settings, Polarity polarity) {
			this.settings = settings;
			this.id = new PortalId(PortalHelper.this.key, polarity);
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

			if (PortalShot.perform(this.id, level, pos, direction, yRot) instanceof PortalShot.Success success) {
				success.place(this.settings);
			}
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
			PortalShot.Source source = PortalShot.Source.forPlacingOn(surface, normal, yRot);
			this.shootFrom(source.source(), normal.getOpposite(), yRot);
		}

		public void assertPresent(double expectedX, double expectedY, double expectedZ, Direction expectedNormal) {
			PortalData portal = this.getPortal().orElseThrow(
					() -> new GameTestAssertException("Expected " + this.id.polarity() + " portal with key " + PortalHelper.this.key + ", got nothing")
			);

			Vec3 origin = PortalHelper.this.helper.relativeVec(portal.origin());
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
				throw new GameTestAssertException("Did not expect " + this.id.polarity() + " portal with key " + PortalHelper.this.key);
		}

		private Optional<PortalData> getPortal() {
			ServerPortalManager manager = PortalHelper.this.helper.getLevel().portalManager();
			return manager.getPortalOptional(this.id).map(PortalReference::get).map(portal -> portal.data);
		}
	}
}
