package io.github.fusionflux.portalcubed.content.portal.collision;

import java.util.List;
import java.util.Objects;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
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
public final class RelevantPortals {
	private final Entity entity;

	// null before the first time update is called
	private AABB lastBounds;
	private Vec3 lastVelocity;
	private List<PortalInstance.Holder> cached;

	public RelevantPortals(Entity entity) {
		this.entity = entity;
	}

	public List<PortalInstance.Holder> get() {
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

	private boolean isIrrelevant(PortalInstance.Holder holder) {
		if (holder.opposite().isEmpty())
			return true;

		PortalInstance portal = holder.portal();
		return !portal.seesModifiedCollision(this.entity);
	}
}
