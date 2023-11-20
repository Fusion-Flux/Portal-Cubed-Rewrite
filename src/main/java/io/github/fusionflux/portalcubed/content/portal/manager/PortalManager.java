package io.github.fusionflux.portalcubed.content.portal.manager;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.collision.CollisionManager;
import io.github.fusionflux.portalcubed.content.portal.storage.PortalStorage;
import io.github.fusionflux.portalcubed.content.portal.storage.SectionPortalStorage;
import io.github.fusionflux.portalcubed.framework.extension.LevelExt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import io.github.fusionflux.portalcubed.framework.util.TransformUtils;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class PortalManager {
	private final Level level;
	protected final PortalStorage storage = new SectionPortalStorage();
	protected final CollisionManager collisionManager;

	public static PortalManager of(Level level) {
		return ((LevelExt) level).pc$portalManager();
	}

	public PortalManager(Level level) {
		this.level = level;
		this.collisionManager = new CollisionManager(this, level);
	}

	public CollisionManager getCollisionManager() {
		return collisionManager;
	}

	public List<Portal> allPortals() {
		return storage.all();
	}

	@Nullable
	public Portal getPortalByNetId(int id) {
		return storage.getByNetId(id);
	}

	public Set<Portal> getPortalsAt(BlockPos pos) {
		return storage.findPortalsInBox(new AABB(pos).inflate(0.1)).collect(Collectors.toSet());
	}

	public PortalPair getPortalsOf(Player player) {
		return getPortalsOf(player.getUUID());
	}

	public PortalPair getPortalsOf(UUID playerId) {
		return storage.getPortalsOf(playerId);
	}

	@Nullable
	public PortalHitResult clipPortal(Vec3 start, Vec3 end) {
		return storage.findPortalsInBox(new AABB(start, end))
                .map(portal -> this.clipPortal(portal, start, end))
                .filter(Objects::nonNull)
				.min(PortalHitResult.CLOSEST_TO_START)
				.orElse(null);
	}

	@Nullable
	private PortalHitResult clipPortal(Portal portal, Vec3 start, Vec3 end) {
		Portal linked = portal.getLinked();
		if (linked == null) // this shouldn't happen
			return null;
		// portals cannot be interacted with from behind
		Vec3 delta = end.subtract(start);
		if (delta.dot(portal.normal) > 0)
			return null;
		return portal.plane.clip(start, end).map(hit -> {
			Vec3 teleportedHit = PortalTeleportHandler.teleportAbsoluteVecBetween(hit, portal, linked);
			Vec3 remainder = hit.vectorTo(end); // relative offset
			Vec3 teleportedEnd = TransformUtils.apply(remainder,
					// already relative, just transform
					portal.rotation::transformInverse,
					linked.rotation180::transform
			).add(teleportedHit); // derelativize

			return new PortalHitResult(start, teleportedEnd, hit, teleportedHit, portal, linked);
        }).orElse(null);
	}

	public void linkPortals(PortalPair portals) {
		if (portals.primary().isPresent() && portals.secondary().isPresent()) {
			this.linkPortals(portals.primary().get(), portals.secondary().get());
		}
	}

	public void linkPortals(Portal a, Portal b) {
		this.unlinkPortal(a);
		this.unlinkPortal(b);
		a.setLinked(b);
		b.setLinked(a);
		collisionManager.handlePortalLink(a, b);
	}

	public void unlinkPortal(Portal portal) {
		Portal linked = portal.getLinked();
		if (linked != null) {
			portal.setLinked(null);
			linked.setLinked(null);
			collisionManager.handlePortalUnlink(portal, linked);
		}
	}
}
