package io.github.fusionflux.portalcubed.content.portal.collision;

import java.util.List;
import java.util.function.Predicate;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.manager.listener.PortalChangeListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class RePortaler implements PortalChangeListener {
	public static final double SPEED = 0.5;

	public static final Predicate<Entity> REPORTALABLE = EntitySelector.NO_SPECTATORS.and(entity -> {
		if (entity.noPhysics)
			return false;

		if (entity.level().isClientSide()) {
			return entity instanceof Player player && player.isLocalPlayer();
		}

		return true;
	});

	private final Level level;

	public RePortaler(Level level) {
		this.level = level;
	}

	@Override
	public void portalModified(Portal oldPortal, PortalReference reference) {
		this.rePortal(oldPortal);
	}

	@Override
	public void portalRemoved(PortalReference reference, Portal portal) {
		this.rePortal(portal);
	}

	private void rePortal(Portal portal) {
		List<Entity> entities = this.findEntitiesToPush(portal);
		if (entities.isEmpty())
			return;

		Vec3 velocity = portal.normal.scale(SPEED);
		for (Entity entity : entities) {
			entity.addDeltaMovement(velocity);
			entity.hasImpulse = true;
		}
	}

	private List<Entity> findEntitiesToPush(Portal portal) {
		AABB area = portal.quad.containingBox();
		List<Entity> entities = this.level.getEntities((Entity) null, area, REPORTALABLE);

		entities.removeIf(entity -> {
			AABB bounds = entity.getBoundingBox();
			return !portal.quad.intersects(bounds);
		});

		return entities;
	}
}
