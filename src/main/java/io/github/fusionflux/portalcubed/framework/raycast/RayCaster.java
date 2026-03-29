package io.github.fusionflux.portalcubed.framework.raycast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.interaction.PortalInteractionUtils;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.PortalLookup;
import io.github.fusionflux.portalcubed.content.portal.ref.HitPortal;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions.PortalMode;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.mixin.portals.EntityGetterMixin;
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
		List<Entity> intersecting = this.level.getEntities((Entity) null, point, predicate);
		for (Entity entity : intersecting) {
			// it's possible there's multiple, but we'll just go with the first hittable one
			if (this.canHitIntersectingEntity(entity, context, predicate)) {
				return new RaycastResult.Entity(this.currentStart, entity);
			}
		}

		// we only provide the context entity on the first step, since we want to avoid hitting it there, but hit it on further steps
		Entity hitContext = this.isOnFirstStep() ? context : null;
		float expansion = this.options.entityExpansion();

		EntityHitResult directResult = ProjectileUtil.getEntityHitResult(this.level, hitContext, this.currentStart, this.currentLimitedEnd, area, predicate, expansion);
		RaycastResult.Entity proxyResult = this.clipEntityProxyHitboxes(area, predicate, expansion);

		if (directResult == null) {
			return proxyResult;
		} else if (proxyResult == null) {
			return new RaycastResult.Entity(directResult);
		} else {
			double directDistance = directResult.getLocation().distanceTo(this.currentStart);
			double proxyDistance = proxyResult.pos.distanceTo(this.currentStart);
			return proxyDistance < directDistance ? proxyResult : new RaycastResult.Entity(directResult);
		}
	}

	/// [EntityGetterMixin#addProxyHitboxes]
	@Nullable
	@SuppressWarnings("JavadocReference")
	private RaycastResult.Entity clipEntityProxyHitboxes(AABB area, Predicate<Entity> predicate, float expansion) {
		AABB portalArea = area.inflate(1);
		Set<PortalReference> portals = this.level.portalManager().lookup().getPortals(portalArea);
		if (portals.isEmpty())
			return null;

		Vec3 closestHitPos = null;
		Vec3 relativeClosestHitPos = null;
		double closestDistanceSqr = Double.MAX_VALUE;
		Entity closestHitEntity = null;

		for (PortalReference portal : portals) {
			Optional<PortalReference> maybeOpposite = portal.opposite();
			if (maybeOpposite.isEmpty())
				continue;

			PortalReference linked = maybeOpposite.get();
			SinglePortalTransform transform = new SinglePortalTransform(portal.get(), linked.get());
			OBB transformedArea = transform.apply(area);
			List<Entity> entities = PortalInteractionUtils.getEntitiesIntersectingPortal(this.level, null, transformedArea, linked.get(), predicate);
			if (entities.isEmpty())
				continue;

			Vec3 transformedStart = transform.applyAbsolute(this.currentStart);
			Vec3 transformedEnd = transform.applyAbsolute(this.currentLimitedEnd);

			for (Entity entity : entities) {
				// based on ProjectileUtil.getEntityHitResult
				AABB bounds = inflate(entity.getBoundingBox(), expansion);
				Optional<Vec3> hit = bounds.clip(transformedStart, transformedEnd);
				if (hit.isEmpty())
					continue;

				Vec3 hitPos = hit.get();
				double distSqr = transformedStart.distanceToSqr(hitPos);

				if (distSqr < closestDistanceSqr) {
					closestHitPos = transform.inverse().applyAbsolute(hitPos);
					relativeClosestHitPos = hitPos;
					closestDistanceSqr = distSqr;
					closestHitEntity = entity;
				}
			}
		}

		if (closestHitPos == null)
			return null;

		Objects.requireNonNull(closestHitEntity, "entity");
		Objects.requireNonNull(relativeClosestHitPos, "relative pos");
		return new RaycastResult.Entity(closestHitPos, closestHitEntity, relativeClosestHitPos);
	}

	private boolean canHitIntersectingEntity(Entity entity, @Nullable Entity context, Predicate<Entity> predicate) {
		if (entity == context && this.isOnFirstStep()) {
			// we can't hit the context entity on the first portal step or else we'll stop instantly
			return false;
		}

		return predicate.test(entity);
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

	private boolean isOnFirstStep() {
		return this.hitPortals.isEmpty();
	}

	private static AABB inflate(AABB bounds, double amount) {
		return amount == 0 ? bounds : bounds.inflate(amount);
	}
}
