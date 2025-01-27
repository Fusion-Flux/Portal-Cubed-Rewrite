package io.github.fusionflux.portalcubed.framework.construct;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

public final class ConstructManager extends SimpleJsonResourceReloadListener<ConstructSet> implements IdentifiableResourceReloadListener {
	public static final ResourceLocation ID = PortalCubed.id("constructs");
	public static final FileToIdConverter CONVERTER = FileToIdConverter.json("construct_set");

	public static final ConstructManager INSTANCE = new ConstructManager();

	private final BiMap<ResourceLocation, ConstructSet> constructSets = HashBiMap.create();
	private final Map<TagKey<Item>, List<ConstructSet>> byMaterial = new IdentityHashMap<>();

	private ConstructManager() {
		super(ConstructSet.CODEC, CONVERTER);
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	protected void apply(Map<ResourceLocation, ConstructSet> sets, ResourceManager manager, ProfilerFiller profiler) {
		this.reset();
		sets.forEach(this::addConstruct);
		this.sortConstructs();
	}

	public void syncToPlayer(ServerPlayer player) {
		if (player.server.isSingleplayerOwner(player.getGameProfile())) {
			// in LAN, don't sync to self
			// but since there's no sync, the preview needs to be reloaded separately
			PortalCubedPackets.sendToClient(player, ReloadConstructPreview.INSTANCE);
		} else {
			ConstructSyncPacket packet = new ConstructSyncPacket(this.constructSets);
			PortalCubedPackets.sendToClient(player, packet);
		}
	}

	public void readFromPacket(ConstructSyncPacket packet) {
		this.reset();
		packet.constructs().forEach(this::addConstruct);
		this.sortConstructs();
	}

	private void sortConstructs() {
		this.byMaterial.values().forEach(list -> list.sort(ConstructSet.BY_SIZE_COMPARATOR));
	}

	private void addConstruct(ResourceLocation id, ConstructSet set) {
		this.constructSets.put(id, set);
		this.byMaterial.computeIfAbsent(
				set.material, $ -> new ArrayList<>()
		).add(set);
	}

	private void reset() {
		this.constructSets.clear();
		this.byMaterial.clear();
	}

	public static void registerEventListeners() {
		// events for syncing to players
		ServerPlayConnectionEvents.JOIN.register(
				(handler, sender, server) -> INSTANCE.syncToPlayer(handler.player)
		);
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
				(server, manager, success) -> server.getPlayerList().getPlayers().forEach(INSTANCE::syncToPlayer)
		);
	}

	// API

	@Nullable
	public ConstructSet getConstructSet(ResourceLocation id) {
		return this.constructSets.get(id);
	}

	@Nullable
	public ResourceLocation getId(ConstructSet set) {
		return this.constructSets.inverse().get(set);
	}

	public Optional<ConstructSet> maybeGetConstructSet(ResourceLocation id) {
		return Optional.ofNullable(this.getConstructSet(id));
	}

	public List<ConstructSet> getConstructSetsForMaterial(TagKey<Item> tag) {
		return this.byMaterial.getOrDefault(tag, List.of());
	}

	@SuppressWarnings("deprecation") // builtInRegistryHolder
	public List<ConstructSet> getConstructSetsForMaterial(Item material) {
		return material.builtInRegistryHolder().tags()
				.map(this::getConstructSetsForMaterial)
				.flatMap(List::stream)
				.toList();
	}

	public Set<TagKey<Item>> getMaterials() {
		return this.byMaterial.keySet();
	}
}
