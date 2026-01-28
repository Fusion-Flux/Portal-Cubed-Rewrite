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
import io.github.fusionflux.portalcubed.framework.util.Angle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * An attempt to place a portal in the world by shooting it. May or may not have succeeded.
 */
public sealed interface PortalShot {
	// we need to break the raycast into steps, since at large distances
	// it becomes noticeably imprecise, hitting things when it shouldn't
	int MAX_CLIP_STEP = 32;

	Predicate<Entity> BLOCKS_PORTAL_SHOTS = EntitySelector.NO_SPECTATORS.and(
			entity -> entity.getType().is(PortalCubedEntityTags.BLOCKS_PORTAL_SHOTS)
	);

	/**
	 * @return the hit result of the raycast performed by this shot
	 */
	HitResult hit();

	/**
	 * Create the particle trail left by this shot.
	 */
	default void createTrail(ServerLevel level, Vec3 source, PortalSettings settings) {
		int color = settings.color().getOpaque(level.getGameTime());
		level.sendParticles(
				new CustomTrailParticleOption(PortalCubedParticles.PORTAL_PROJECTILE, this.hit().getLocation(), color, 3),
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
	 * @param hit a hit result with the {@link HitResult.Type#MISS miss} type
	 */
	record Missed(BlockHitResult hit) implements PortalShot {
		public Missed {
			if (hit.getType() != HitResult.Type.MISS) {
				throw new IllegalArgumentException("Non-miss HitResult: " + hit);
			}
		}
	}

	/**
	 * A portal shot that hit something, but failed to find a valid placement.
	 * @param hit a hit result with either the BLOCK or ENTITY type.
	 */
	record Failed(HitResult hit) implements PortalShot {
		public Failed {
			if (hit.getType() == HitResult.Type.MISS) {
				throw new IllegalArgumentException("A MISS result should be Missed, not Failed: " + hit);
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
		private final BlockHitResult hit;

		private Success(PortalId id, ServerLevel level, PortalPlacement placement, BlockHitResult hit) {
			if (hit.getType() == HitResult.Type.MISS || hit.isInside()) {
				throw new IllegalArgumentException("Incorrect HitResult for Success: " + hit);
			}

			this.id = id;
			this.level = level;
			this.placement = placement;
			this.hit = hit;
		}

		@Override
		public BlockHitResult hit() {
			return this.hit;
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

		HitResult hit = clip(level, source, direction, maxRange);
		if (hit instanceof EntityHitResult)
			return new Failed(hit);

		if (!(hit instanceof BlockHitResult blockHit)) {
			throw new IllegalStateException("Weird HitResult: " + hit);
		}

		if (hit.getType() == HitResult.Type.MISS) {
			return new Missed(blockHit);
		} else if (blockHit.isInside()) {
			return new Failed(hit);
		}

		Direction face = blockHit.getDirection();
		Angle bias = getBias(direction, face);

		PortalPlacement placement = PortalBumper.findValidPlacement(
				shooting, level, hit.getLocation(), yRot, blockHit.getBlockPos(), face, bias, null
		);

		return placement == null ? new Failed(hit) : new Success(shooting, level, placement, blockHit);
	}

	@Nullable
	private static Angle getBias(Vec3 direction, Direction face) {
		if (!face.getAxis().isHorizontal())
			return null;

		Direction left = face.getClockWise();
		double dot = direction.dot(left.getUnitVec3());
		return dot > 0 ? Angle.R270 : Angle.R90;
	}

	private static HitResult clip(ServerLevel level, Vec3 source, Vec3 direction, double maxRange) {
		double range = Math.min(maxRange, level.getGameRules().getInt(PortalCubedGameRules.PORTAL_SHOT_RANGE_LIMIT));

		double distance = range;
		while (distance > 0) {
			double step = Math.min(distance, MAX_CLIP_STEP);
			HitResult result = subClip(level, source, direction, step);
			if (result.getType() != HitResult.Type.MISS)
				return result;

			distance -= step;
			source = result.getLocation();
		}

		// no hit. create a miss result at the maximum distance
		Vec3 end = source.add(direction.scale(range));
		Direction nearestDirection = Direction.getApproximateNearest(direction);
		BlockPos endPos = BlockPos.containing(end);
		return BlockHitResult.miss(end, nearestDirection, endPos);
	}

	private static HitResult subClip(ServerLevel level, Vec3 source, Vec3 direction, double distance) {
		Vec3 to = source.add(direction.scale(distance));

		ClipContext ctx = new ClipContext(source, to, PortalShotClipContextMode.get(), ClipContext.Fluid.NONE, PortalCollisionContext.INSTANCE);
		ctx.pc$setIgnoreInteractionOverride(true);

		BlockHitResult blockHit = level.clip(ctx);

		AABB area = new AABB(source, to);
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, null, source, to, area, BLOCKS_PORTAL_SHOTS, 0);
		if (entityHit == null)
			return blockHit;

		if (blockHit.getType() == HitResult.Type.MISS)
			return entityHit;

		double blockHitDistance = blockHit.getLocation().distanceTo(source);
		double entityHitDistance = entityHit.getLocation().distanceTo(source);
		return blockHitDistance <= entityHitDistance ? blockHit : entityHit;
	}
}
