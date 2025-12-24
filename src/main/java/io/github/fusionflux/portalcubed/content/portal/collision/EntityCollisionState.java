package io.github.fusionflux.portalcubed.content.portal.collision;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Consumer;

import io.github.fusionflux.portalcubed.framework.shape.OBB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class EntityCollisionState {
	public final Entity entity;
	public final Vec3 idealMotion;

	private Set<OBB> colliders;

	public EntityCollisionState(Entity entity, Vec3 idealMotion) {
		this.entity = entity;
		this.idealMotion = idealMotion;
		this.colliders = Collections.newSetFromMap(new IdentityHashMap<>());
	}

	public void addColliders(Collection<OBB> colliders) {
		if (this.colliders != null) {
			this.colliders.addAll(colliders);
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
