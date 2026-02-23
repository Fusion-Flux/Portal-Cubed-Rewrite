package io.github.fusionflux.portalcubed.content.portal.clip;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import io.github.fusionflux.portalcubed.content.PortalCubedParticles;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.placement.PortalBumper;
import io.github.fusionflux.portalcubed.content.portal.placement.PortalCollisionContext;
import io.github.fusionflux.portalcubed.content.portal.placement.PortalPlacement;
import io.github.fusionflux.portalcubed.content.portal.placement.PortalShotClipContextMode;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.NonePortalValidator;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.PortalValidator;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.StandardPortalValidator;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.particle.CustomTrailParticleOption;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import io.github.fusionflux.portalcubed.framework.util.Angle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.phys.Vec3;

/**
 * An attempt to place a portal in the world by shooting it. May or may not have succeeded.
 */
public sealed interface PortalShot {
	Predicate<Entity> BLOCKS_PORTAL_SHOTS = EntitySelector.NO_SPECTATORS.and(
			entity -> entity.getType().is(PortalCubedEntityTags.BLOCKS_PORTAL_SHOTS)
	);

	RaycastOptions RAYCAST_OPTIONS = RaycastOptions.DEFAULT.edit()
			.blocks(PortalShotClipContextMode.get())
			.entities(BLOCKS_PORTAL_SHOTS)
			.portals(RaycastOptions.PortalMode.IGNORE)
			.collisionContext(PortalCollisionContext.INSTANCE)
			.ignoreInteractionOverride(true)
			.build();

	/**
	 * @return the {@link RaycastResult} of the raycast this shot performed
	 */
	RaycastResult result();

	/**
	 * Create the particle trail left by this shot.
	 */
	default void createTrail(ServerLevel level, Vec3 source, PortalSettings settings) {
		int color = settings.color().getOpaque(level.getGameTime());
		level.sendParticles(
				new CustomTrailParticleOption(PortalCubedParticles.PORTAL_PROJECTILE, this.result().pos, color, 3),
				source.x, source.y, source.z, 1, 0, 0, 0, 0
		);
	}

	record Source(Vec3 source, Vec3 direction, float yRot, double maxRange) {
		public PortalShot shoot(PortalId shooting, ServerLevel level) {
			return PortalShot.perform(shooting, level, this.source, this.direction, this.yRot, this.maxRange);
		}

		public static Source forPlacingOn(BlockPos pos, Direction facing, float yRot) {
			// we want the bottom of the portal to be centered on the surface, so we need to shoot from 0.5 blocks "up"
			Quaternionf rotation = PortalData.normalToRotation(facing, 0);

			Angle angle = PortalData.normalToFlatRotation(facing, yRot);
			if (angle.rad() != 0) {
				rotation.rotateY(-angle.radF());
			}

			Vector3f offsetUp = rotation.transformUnit(new Vector3f(0, 0, 0.5f));

			Vec3 offsetFromWall = facing.getUnitVec3().scale(0.75);
			Vec3 source = Vec3.atCenterOf(pos).add(offsetFromWall).add(offsetUp.x, offsetUp.y, offsetUp.z);
			Vec3 direction = facing.getUnitVec3().scale(-1);
			return new Source(source, direction, yRot, 1);
		}
	}

	/**
	 * A portal shot that completely missed, hitting no blocks.
	 */
	record Missed(RaycastResult.Missed result) implements PortalShot {}

	/**
	 * A portal shot that hit something, but failed to find a valid placement.
	 * @param result either a {@link RaycastResult.Block}, a {@link RaycastResult.Entity}, or a {@link RaycastResult.WorldBorder}
	 */
	record Failed(RaycastResult result) implements PortalShot {
		public Failed {
			if (!(result instanceof RaycastResult.Block || result instanceof RaycastResult.Entity || result instanceof RaycastResult.WorldBorder)) {
				throw new IllegalArgumentException("Result should be a Block, Entity, or WorldBorder: " + result);
			}
		}
	}

	/**
	 * A portal shot that found a valid placement.
	 */
	final class Success implements PortalShot {
		public final PortalPlacement placement;

		private final PortalId id;
		private final ServerLevel level;
		private final RaycastResult.Block result;

		private Success(PortalId id, ServerLevel level, PortalPlacement placement, RaycastResult.Block result) {
			if (result.isInside) {
				throw new IllegalArgumentException("Incorrect RaycastResult for Success: " + result);
			}

			this.id = id;
			this.level = level;
			this.placement = placement;
			this.result = result;
		}

		@Override
		public RaycastResult.Block result() {
			return this.result;
		}

		/**
		 * Place a portal with the given settings at the location of this shot.
		 */
		public void place(PortalSettings settings) {
			PortalValidator validator = settings.validate() ? this.createValidator() : NonePortalValidator.INSTANCE;
			PortalData data = PortalData.createWithSettings(this.level, this.placement.pos(), this.placement.rotation(), validator, settings);
			this.level.portalManager().createPortal(this.id, data);
		}

		public PortalValidator createValidator() {
			return new StandardPortalValidator(this.placement.rotationAngle());
		}
	}

	/**
	 * Perform a portal shot with no range limit, besides the gamerule.
	 * @see #perform(PortalId, ServerLevel, Vec3, Vec3, float, double)
	 */
	static PortalShot perform(PortalId shooting, ServerLevel level, Vec3 source, Vec3 direction, float yRot) {
		return perform(shooting, level, source, direction, yRot, Double.MAX_VALUE);
	}

	/**
	 * Perform a portal shot by raycasting from {@code source} along {@code direction}.
	 * @param shooting the ID of the portal that is being shot
	 * @param direction a normalized vector pointing in the direction of travel
	 * @param yRot the Y rotation of the shooter, used for rotating the portal
	 * @param maxRange the maximum distance the shot can travel before giving up
	 */
	static PortalShot perform(PortalId shooting, ServerLevel level, Vec3 source, Vec3 direction, float yRot, double maxRange) {
		if (maxRange <= 0) {
			throw new IllegalArgumentException("Maximum range must be >0");
		} else if (direction.lengthSqr() == 0) {
			throw new IllegalArgumentException("Direction vector has 0 length");
		} else {
			direction = direction.normalize();
		}

		double range = Math.min(maxRange, level.getGameRules().getInt(PortalCubedGameRules.PORTAL_SHOT_RANGE_LIMIT));
		RaycastResult.VanillaConvertible result = RAYCAST_OPTIONS.raycast(level, source, direction, range).assertNotPortal();

		return switch (result) {
			case RaycastResult.Missed missed -> new Missed(missed);
			case RaycastResult.Entity entity -> new Failed(entity);
			case RaycastResult.WorldBorder worldBorder -> new Failed(worldBorder);
			case RaycastResult.Block block -> {
				if (block.isInside) {
					yield new Failed(block);
				}

				Angle bias = getBias(direction, block.face);

				PortalPlacement placement = PortalBumper.findValidPlacement(shooting, level, block.pos, yRot, block.blockPos, block.face, bias, null);
				yield placement == null ? new Failed(block) : new Success(shooting, level, placement, block);
			}
		};
	}

	@Nullable
	private static Angle getBias(Vec3 direction, Direction face) {
		if (!face.getAxis().isHorizontal())
			return null;

		Direction left = face.getClockWise();
		double dot = direction.dot(left.getUnitVec3());
		return dot > 0 ? Angle.R270 : Angle.R90;
	}
}
