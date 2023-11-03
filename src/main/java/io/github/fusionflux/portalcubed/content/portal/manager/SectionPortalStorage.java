package io.github.fusionflux.portalcubed.content.portal.manager;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class SectionPortalStorage implements PortalStorage {
	private final Long2ObjectMap<List<Portal>> sections;

	public SectionPortalStorage() {
		this.sections = new Long2ObjectOpenHashMap<>();
	}

	@Override
	public void addPortal(Portal portal) {
		AABB planeBox = new AABB(portal.plane().pos1(), portal.plane().pos2());
		this.sectionsInBox(planeBox)
				.mapToObj(pos -> sections.computeIfAbsent(pos, $ -> new ArrayList<>()))
				.forEach(list -> list.add(portal));
	}

	@Override
	public Stream<Portal> findPortalsInBox(AABB box) {
		return sectionsInBox(box)
				.mapToObj(sections::get)
				.filter(Objects::nonNull)
				.flatMap(List::stream);
	}

	private LongStream sectionsInBox(AABB box) {
		return SectionPos.betweenClosedStream(
				Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ),
				Mth.ceil(box.maxX), Mth.ceil(box.maxY), Mth.ceil(box.minZ)
		).mapToLong(SectionPos::asLong);
	}
}
