package io.github.fusionflux.portalcubed.content.portal;

import io.github.fusionflux.portalcubed.content.portal.storage.PortalStorage;
import io.github.fusionflux.portalcubed.content.portal.storage.SectionPortalStorage;
import io.github.fusionflux.portalcubed.framework.extension.LevelExt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PortalManager {
	private final Level level;
	public final PortalStorage storage;

	public PortalManager(Level level) {
		this.level = level;
		this.storage = new SectionPortalStorage();
	}

	public static PortalManager of(Level level) {
		return ((LevelExt) level).pc$portalManager();
	}

	public void addPortal(Portal portal) {
		this.storage.addPortal(portal);
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
