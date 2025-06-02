package io.github.fusionflux.portalcubed_gametests;

import java.util.Optional;

import org.joml.Quaternionf;
import org.joml.Vector3d;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.content.portal.color.ConstantPortalColor;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunShootContext;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.PortalValidator;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.StandardPortalValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

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
						new PortalSettings(PortalType.ROUND, false, new ConstantPortalColor(primaryColor), false),
						Polarity.PRIMARY
				),
				new SinglePortalHelper(
						helper, key,
						new PortalSettings(PortalType.ROUND, false, new ConstantPortalColor(secondaryColor), false),
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
			Quaternionf rotation = PortalData.normalToRotation(normal, yRot);
			// shift the portal so the bottom half is centered on the surface
			Vector3d offset = rotation.transformUnit(0, 0, normal.getAxis() == Direction.Axis.Y ? -0.5f : 0.5f, new Vector3d());

			// correct for float imprecision that can break surface detection
			switch (normal.getAxis()) {
				case X -> offset.x = 0;
				case Y -> offset.y = 0;
				case Z -> offset.z = 0;
			}

			BlockPos blockPos = this.helper.absolutePos(surface);
			Vec3 pos = Vec3.atCenterOf(blockPos)
					.add(normal.getUnitVec3().scale(0.5))
					.add(offset.x, offset.y, offset.z);

			Vec3 intoWall = normal.getUnitVec3().scale(-1);

			ServerLevel level = this.helper.getLevel();
			BlockState state = level.getBlockState(blockPos);
			VoxelShape shape = state.getCollisionShape(this.helper.getLevel(), blockPos)
					.move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
			BlockHitResult hit = shape.clip(pos, pos.add(intoWall), blockPos);
			if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
				pos = hit.getLocation();
			}

			ServerPortalManager manager = level.portalManager();
			PortalValidator validator = new StandardPortalValidator(PortalData.normalToFlatRotation(normal, yRot));
			manager.createPortal(this.key, this.polarity, PortalData.createWithSettings(level, pos, rotation, validator, this.settings));
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
