package io.github.fusionflux.portalcubed.content.portal.storage;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class SectionPortalStorage implements PortalStorage {
	private final List<Portal> portals;
	private final Long2ObjectMap<List<Portal>> sections;
	private final Int2ObjectMap<Portal> byNetId;
	private final Map<UUID, PortalPair> byPlayer;

	public SectionPortalStorage() {
		this.portals = new ArrayList<>();
		this.sections = new Long2ObjectOpenHashMap<>();
		this.byNetId = new Int2ObjectOpenHashMap<>();
		this.byPlayer = new HashMap<>();
	}

	@Override
	public void addPortal(Portal portal) {
		this.portals.add(portal);
		this.byNetId.put(portal.netId, portal);
		this.modifyPortalsOf(portal.owner, portals -> portals.withPortal(portal));
		sectionsInBox(portal.plane).forEach(
				section -> sections.computeIfAbsent(section, $ -> new ArrayList<>()).add(portal)
		);
	}

	@Override
	public void removePortal(Portal portal) {
		if (this.portals.remove(portal)) {
			this.byNetId.remove(portal.netId);
			this.modifyPortalsOf(portal.owner, portals -> portals.withoutPortal(portal));
			sectionsInBox(portal.plane).forEach(section -> {
				List<Portal> portals = sections.get(section);
				if (portals != null) {
					portals.remove(portal);
					if (portals.isEmpty()) {
						sections.remove(section);
					}
				}
			});
		}
	}

	@Override
	@Nullable
	public Portal getByNetId(int id) {
		return byNetId.get(id);
	}

	@Override
	public PortalPair getPortalsOf(UUID owner) {
		return byPlayer.getOrDefault(owner, PortalPair.EMPTY);
	}

	@Override
	public Stream<Portal> findPortalsInBox(AABB box) {
		return sectionsInBox(box)
				.mapToObj(sections::get)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.filter(portal -> portal.plane.intersects(box));
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

	private void modifyPortalsOf(UUID owner, UnaryOperator<PortalPair> function) {
		PortalPair pair = byPlayer.getOrDefault(owner, PortalPair.EMPTY);
		PortalPair modified = function.apply(pair);
		if (modified == null) {
			byPlayer.remove(owner);
		} else {
			byPlayer.put(owner, modified);
		}
	}
}
