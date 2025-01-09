package io.github.fusionflux.portalcubed.framework.construct;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

public class ConstructManager extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloader {
	public static final ResourceLocation ID = PortalCubed.id("constructs");
	public static final String DIR = "construct_sets";

	private static final Logger logger = LoggerFactory.getLogger(ConstructManager.class);
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static ConstructManager INSTANCE = new ConstructManager();

	private final BiMap<ResourceLocation, ConstructSet> constructSets = HashBiMap.create();
	private final Map<TagKey<Item>, List<ConstructSet>> byMaterial = new IdentityHashMap<>();

	private ConstructManager() {
		super(gson, DIR);
	}

	@Override
	@NotNull
	public ResourceLocation getQuiltId() {
		return ID;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> cache, ResourceManager manager, ProfilerFiller profiler) {
		this.reset();
		cache.forEach(
				(id, json) -> tryParseConstruct(id, JsonOps.INSTANCE, json).ifPresent(this::addConstruct)
		);
		this.sortConstructs();
	}

	public void syncToPlayer(ServerPlayer player) {
		if (player.server.isSingleplayerOwner(player.getGameProfile()))
			return; // in LAN, don't sync to self

		// build packet
		ConstructSyncPacket packet = new ConstructSyncPacket(this.constructSets);
		PortalCubedPackets.sendToClient(player, packet);
	}

	public void readFromPacket(ConstructSyncPacket packet) {
		this.reset();
		packet.getConstructs().forEach(this::addConstruct);
		this.sortConstructs();
	}

	protected static <T> Optional<ConstructSet.Holder> tryParseConstruct(ResourceLocation id, DynamicOps<T> ops, T data) {
		ConstructSet constructSet = ConstructSet.CODEC.parse(ops, data).get().map(
				Function.identity(),
				partial -> {
					logger.error("Failed to parse construct {}: {}", id, partial.message());
					return null;
				}
		);

		return constructSet == null ? Optional.empty() : Optional.of(new ConstructSet.Holder(id, constructSet));
	}

	private void sortConstructs() {
		this.byMaterial.values().forEach(list -> list.sort(ConstructSet.BY_SIZE_COMPARATOR));
	}

	private void addConstruct(ConstructSet.Holder holder) {
		ConstructSet constructSet = holder.constructSet();
		this.constructSets.put(holder.id(), constructSet);
		this.byMaterial.computeIfAbsent(
				constructSet.material, $ -> new ArrayList<>()
		).add(constructSet);
	}

	private void reset() {
		this.constructSets.clear();
		this.byMaterial.clear();
	}

	public static void init() {
		ResourceLoader.get(PackType.SERVER_DATA).registerReloader(INSTANCE);

		// events for syncing to players
		ServerPlayConnectionEvents.JOIN.register(
				(handler, sender, server) -> INSTANCE.syncToPlayer(handler.player)
		);
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register(
				ctx -> {
					MinecraftServer server = ctx.server();
					if (server != null) {
						server.getPlayerList().getPlayers().forEach(INSTANCE::syncToPlayer);
					}
				}
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
