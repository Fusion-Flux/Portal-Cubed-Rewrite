package io.github.fusionflux.portalcubed.content.portal.collision;

import java.util.Objects;
import java.util.Set;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.manager.listener.PortalChangeListener;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Tracks the portals that are considered "relevant" to an entity.
 * A portal is relevant if all the following are true:
 * <ul>
 *     <li>the portal intersects the entity's velocity-expanded bounding box</li>
 *     <li>the portal is linked</li>
 *     <li>the entity's bounds intersect the region in front of the portal</li>
 * </ul>
 */
public final class RelevantPortals implements PortalChangeListener {
	private final Entity entity;

	// null before the first time update is called
	private AABB lastBounds;
	private Vec3 lastVelocity;
	private Set<PortalReference> cached;

	public RelevantPortals(Entity entity) {
		this.entity = entity;
	}

	public Set<PortalReference> get() {
		if (this.needsUpdate())
			this.update();

		return this.cached;
	}

	private void update() {
		this.lastBounds = this.entity.getBoundingBox();
		this.lastVelocity = this.entity.getDeltaMovement();
		AABB area = this.lastBounds.expandTowards(this.lastVelocity);
		this.cached = this.entity.level().portalManager().lookup().getPortals(area);
		this.cached.removeIf(this::isIrrelevant);
	}

	private boolean needsUpdate() {
		return this.cached == null
				|| !Objects.equals(this.lastBounds, this.entity.getBoundingBox())
				|| !Objects.equals(this.lastVelocity, this.entity.getDeltaMovement());
	}

	private boolean isIrrelevant(PortalReference reference) {
		if (reference.opposite().isEmpty())
			return true;

		Portal portal = reference.get();
		return !portal.seesModifiedCollision(this.entity);
	}

	private void invalidate() {
		this.cached = null;
	}

	@Override
	public void portalPairChanged(PortalPair oldPair, PortalPair newPair) {
		this.invalidate();
	}
}
