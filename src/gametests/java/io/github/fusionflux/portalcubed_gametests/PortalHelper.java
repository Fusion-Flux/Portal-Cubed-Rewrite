package io.github.fusionflux.portalcubed_gametests;

import java.util.Optional;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.PortalShape;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunShootContext;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

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
			new PortalGunShootContext(this.key, this.helper.getLevel(), this.helper.absoluteVec(from), facing.getUnitVec3(), yRot)
					.shoot(Optional.empty(), this.polarity, this.settings);
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
			Vector3f baseOffset = new Vector3f(0, 0.5f, 0);
			Vector3f offset = rotation.transform(baseOffset);

			BlockPos blockPos = this.helper.absolutePos(surface);
			Vec3 pos = Vec3.atCenterOf(blockPos)
					.add(normal.getUnitVec3().scale(0.5))
					.add(offset.x, offset.y, offset.z);

			Vec3 intoWall = normal.getUnitVec3().scale(-1);

			BlockState state = this.helper.getLevel().getBlockState(blockPos);
			VoxelShape shape = state.getCollisionShape(this.helper.getLevel(), blockPos);
			BlockHitResult hit = shape.clip(pos, pos.add(intoWall), blockPos);
			if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
				pos = hit.getLocation();
			}

			ServerPortalManager manager = this.helper.getLevel().portalManager();
			manager.createPortal(this.key, this.polarity, new PortalData(pos, rotation, this.settings));
		}
	}
}
