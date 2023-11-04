package io.github.fusionflux.portalcubed.content.portal.storage;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class SectionPortalStorage implements PortalStorage {
	private final List<Portal> portals;
	private final Long2ObjectMap<List<Portal>> sections;

	public SectionPortalStorage() {
		this.portals = new ArrayList<>();
		this.sections = new Long2ObjectOpenHashMap<>();
	}

	@Override
	public void addPortal(Portal portal) {
		this.portals.add(portal);
		Vec3 pos = portal.origin;
		long section = SectionPos.asLong(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z));
		sections.computeIfAbsent(section, $ -> new ArrayList<>()).add(portal);
	}

	@Override
	public void removePortal(Portal portal) {
		if (this.portals.remove(portal)) {
			Vec3 pos = portal.origin;
			long section = SectionPos.asLong(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z));
			List<Portal> portals = sections.get(section);
			if (portals != null) {
				portals.remove(portal);
				if (portals.isEmpty()) {
					sections.remove(section);
				}
			}
		}
	}

	@Override
	public Stream<Portal> findPortalsInBox(AABB box) {
		return sectionsInBox(box)
				.mapToObj(sections::get)
				.filter(Objects::nonNull)
				.flatMap(List::stream);
	}

	@Override
	public List<Portal> all() {
		return portals;
	}

	private LongStream sectionsInBox(AABB box) {
		return SectionPos.betweenClosedStream(
				Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ),
				Mth.ceil(box.maxX), Mth.ceil(box.maxY), Mth.ceil(box.minZ)
		).mapToLong(SectionPos::asLong);
	}
}
