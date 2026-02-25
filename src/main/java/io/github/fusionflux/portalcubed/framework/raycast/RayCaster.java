package io.github.fusionflux.portalcubed.framework.raycast;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.manager.lookup.PortalLookup;
import io.github.fusionflux.portalcubed.content.portal.ref.HitPortal;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions.PortalMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

// RayCaster? I hardly know 'er! But I hear she's related to Mr. Tracing.
final class RayCaster {
	// we need to break the block raycast into steps, since at large distances
	// it becomes noticeably imprecise, hitting things when it shouldn't
	private static final int maxBlockStepDistance = 32;

	private final Level level;
	private final Vec3 originalStart;
	private final RaycastOptions options;
	private final List<PortalPath.Entry> hitPortals;

	private boolean used;

	private RaycastResult closestResult;
	private Vec3 currentStart;
	private Vec3 currentIdealEnd;
	private Vec3 currentLimitedEnd;

	RayCaster(Level level, Vec3 start, Vec3 end, RaycastOptions options) {
		this.level = level;
		this.originalStart = start;
		this.currentStart = start;
		this.currentIdealEnd = end;
		this.currentLimitedEnd = end;
		this.options = options;
		this.hitPortals = this.passThroughPortals() ? new ArrayList<>() : List.of();
	}

	public RaycastResult raycast() {
		Validate.isTrue(!this.used, "RayCaster already used");
		this.used = true;

		while (true) {
			this.handleResult(this.performSimpleLevelClip());
			this.handleResult(this.performSimpleEntityHit());
			this.handleResult(this.performSimplePortalHit());

			if (this.passThroughPortals() && this.closestResult instanceof RaycastResult.Portal portal) {
				Optional<PortalReference> maybeLinked = portal.portal.opposite();
				if (maybeLinked.isPresent()) {
					// linked pair, teleport and loop
					PortalReference linked = maybeLinked.get();
					SinglePortalTransform transform = new SinglePortalTransform(portal.portal.get(), linked.get());
					this.currentStart = transform.applyAbsolute(portal.pos);
					this.currentIdealEnd = transform.applyAbsolute(this.currentIdealEnd);
					this.currentLimitedEnd = this.currentIdealEnd;
					this.closestResult = null;

					this.hitPortals.add(new PortalPath.Entry(new HitPortal(portal.portal, portal.pos), new HitPortal(linked, this.currentStart)));

					continue;
				}
			}

			// didn't teleport, stop
			break;
		}

		if (this.closestResult == null) {
			// hit nothing, fall back to a miss
			Vec3 reverseDirection = this.currentLimitedEnd.vectorTo(this.currentStart);
			Direction face = Direction.getApproximateNearest(reverseDirection);
			this.closestResult = new RaycastResult.Missed(this.currentLimitedEnd, face);
		}

		if (this.hitPortals.isEmpty()) {
			// passed through no portals, leave result as-is
			return this.filterResult(this.closestResult);
		}

		// passed through portals, build a path
		PortalPath path = PortalPath.of(this.hitPortals);
		return this.filterResult(this.closestResult.withPath(path));
	}

	private void handleResult(@Nullable RaycastResult result) {
		if (result == null)
			return;

		if (this.closestResult == null || result.isCloserToPosThan(this.currentStart, this.closestResult)) {
			this.closestResult = result;
			this.currentLimitedEnd = result.pos;
		}
	}

	private RaycastResult filterResult(RaycastResult result) {
		if (result instanceof RaycastResult.Block block) {
			double distance = this.distanceTravelled(block);
			if (distance > this.options.blockRange()) {
				return new RaycastResult.Missed(block.path, block.pos, block.face);
			}
		} else if (result instanceof RaycastResult.Entity entity) {
			double distance = this.distanceTravelled(entity);
			if (distance > this.options.entityRange()) {
				Vec3 reverseDirection = this.currentLimitedEnd.vectorTo(this.currentStart).normalize();
				Direction face = Direction.getApproximateNearest(reverseDirection);
				return new RaycastResult.Missed(entity.path, entity.pos, face);
			}
		}

		return result;
	}

	private double distanceTravelled(RaycastResult result) {
		return result.path.map(path -> path.distanceThrough(this.originalStart, result.pos))
				.orElseGet(() -> this.originalStart.distanceTo(result.pos));
	}

	private boolean passThroughPortals() {
		return this.options.portalMode() == PortalMode.PASS_THROUGH;
	}

	/**
	 * Performs a simple (non-portal-crossing) raycast through the level, hitting blocks, fluids, and the world border.
	 */
	@Nullable
	private RaycastResult.BlockLike performSimpleLevelClip() {
		if (!this.options.shouldClipLevel())
			return null;

		RaycastResult.BlockLike result = this.clipLevel(null);
		if (result == null)
			return null;

		return this.ignoreHitBlock(result) ? this.clipLevel(result.blockPos) : result;
	}

	// if the raycast immediately hit a block that's part of the portal_interaction_passthrough tag,
	// we need to raycast again, ignoring that block. we do this so interactions aren't blocked by
	// the back faces of facades that portals are placed on.
	private boolean ignoreHitBlock(RaycastResult.BlockLike result) {
		// if hitPortals is empty, then we're not raycasting out of a portal.
		if (this.hitPortals.isEmpty() || !(result instanceof RaycastResult.Block block))
			return false;

		// we're raycasting out of a portal, so currentStart will correspond to a point on that portal's surface.
		// early return if that point is too far from the hit pos, since that means it wasn't hit immediately.
		if (this.currentStart.distanceToSqr(block.pos) > 0.1)
			return false;

		BlockState state = this.level.getBlockState(block.blockPos);
		return state.is(PortalCubedBlockTags.PORTAL_INTERACTION_PASSTHROUGH);
	}

	@Nullable
	private RaycastResult.BlockLike clipLevel(@Nullable BlockPos ignoredPos) {
		Vec3 startToEnd = this.currentStart.vectorTo(this.currentLimitedEnd);
		Vec3 direction = startToEnd.normalize();

		Vec3 stepStart = this.currentStart;
		double distance = startToEnd.length();

		while (distance > 0) {
			double distanceThisStep = Math.min(distance, maxBlockStepDistance);
			Vec3 offset = direction.scale(distanceThisStep);
			Vec3 stepEnd = stepStart.add(offset);

			ClipContext context = this.options.createClipContext(stepStart, stepEnd, ignoredPos);
			BlockHitResult result = this.options.clipLevel(this.level, context);
			if (result.getType() == HitResult.Type.MISS) {
				// no hit, continue to next step
				stepStart = stepEnd;
				distance -= distanceThisStep;
				continue;
			}

			// hit something, done
			return RaycastResult.of(result);
		}

		// no hit during any step
		return null;
	}

	@Nullable
	private RaycastResult.Entity performSimpleEntityHit() {
		if (this.options.entityPredicate().isEmpty())
			return null;

		Predicate<Entity> predicate = this.options.entityPredicate().get();
		Entity context = this.options.contextEntity();
		AABB area = new AABB(this.currentStart, this.currentLimitedEnd).inflate(1);

		// before we raycast, see if there's an entity intersecting the start point.
		// a raycast that starts inside a hitbox will not count as hitting it.
		AABB point = AABB.ofSize(this.currentStart, 0.01, 0.01, 0.01);
		List<Entity> intersecting = this.level.getEntities(context, point, predicate);
		for (Entity entity : intersecting) {
			// it's possible there's multiple, but we'll just go with the first one (that isn't the context entity)
			if (entity != this.options.contextEntity()) {
				return new RaycastResult.Entity(this.currentStart, entity);
			}
		}

		EntityHitResult result = ProjectileUtil.getEntityHitResult(this.level, context, this.currentStart, this.currentLimitedEnd, area, predicate, 0);
		return result == null ? null : new RaycastResult.Entity(result.getLocation(), result.getEntity());
	}

	@Nullable
	private RaycastResult.Portal performSimplePortalHit() {
		PortalMode mode = this.options.portalMode();
		if (mode == PortalMode.IGNORE)
			return null;

		// extend the raycast slightly, since a non-floating portal will be exactly aligned with its surface.
		// if we don't do this, whether it'll be hit or not depends on float imprecision.
		Vec3 actualEnd = Mth.lerp(1.01, this.currentStart, this.currentLimitedEnd);

		boolean hitClosed = mode == PortalMode.HIT;
		PortalLookup lookup = this.level.portalManager().lookup();
		Optional<HitPortal> maybeHit = lookup.clipOnce(this.currentStart, actualEnd, hitClosed);
		if (maybeHit.isEmpty())
			return null;

		HitPortal hit = maybeHit.get();
		return new RaycastResult.Portal(hit.pos(), hit.reference());
	}
}
