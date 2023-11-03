package io.github.fusionflux.portalcubed.content.portal.manager;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class PortalManager {
	private final Level level;
	public final PortalStorage storage;

	public PortalManager(Level level) {
		this.level = level;
		this.storage = new SectionPortalStorage();
	}

	@Nullable
	public PortalPickResult pickPortal(Vec3 start, Vec3 end) {
		return storage.findPortalsInBox(new AABB(start, end))
				.map(portal -> this.pickPortal(portal, start, end))
				.filter(Objects::nonNull)
				.sorted().findFirst().orElse(null);
	}

	@Nullable
	private PortalPickResult pickPortal(Portal portal, Vec3 start, Vec3 end) {
		Vec3 hit = portal.plane().pick(start, end);
		return hit == null ? null : new PortalPickResult(start, end, hit, portal);
	}
}
