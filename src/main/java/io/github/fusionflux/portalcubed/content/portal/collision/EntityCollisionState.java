package io.github.fusionflux.portalcubed.content.portal.collision;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class EntityCollisionState {
	public final Entity entity;
	public final Vec3 idealMotion;
	public final List<PortalInstance.Holder> portals;

	private List<OBB> colliders;

	public EntityCollisionState(Entity entity, Vec3 idealMotion, List<PortalInstance.Holder> portals) {
		this.entity = entity;
		this.idealMotion = idealMotion;
		this.portals = portals;
		this.colliders = new ArrayList<>();
	}

	public void addCollider(OBB box) {
		if (this.colliders != null) {
			this.colliders.add(box);
		}
	}

	public boolean hasColliders() {
		return !this.colliders.isEmpty();
	}

	public void forEachCollider(Consumer<OBB> consumer) {
		this.colliders.forEach(consumer);
	}

	public void stopCollectingColliders() {
		this.colliders = null;
	}
}
