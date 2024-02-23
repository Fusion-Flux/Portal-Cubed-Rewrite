package io.github.fusionflux.portalcubed.framework.construct;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import net.minecraft.world.item.Item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

public class ConstructManager extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloader {
	public static final ResourceLocation ID = PortalCubed.id("constructs");
	public static final String DIR = "cannon_constructs";

	private static final Logger logger = LoggerFactory.getLogger(ConstructManager.class);
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	@SuppressWarnings("SortedCollectionWithNonComparableKeys")
	private static final SortedSet<Construct> emptySet = Collections.unmodifiableSortedSet(new TreeSet<>());

	public static ConstructManager INSTANCE = new ConstructManager();

	private final BiMap<ResourceLocation, Construct> constructs = HashBiMap.create();
	private final Map<Item, SortedSet<Construct>> byMaterial = new HashMap<>();

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
		cache.forEach((id, json) -> tryParseConstruct(id, JsonOps.INSTANCE, json).ifPresent(this::addConstruct));
	}

	public void syncToPlayer(ServerPlayer player) {
		if (player.server.isSingleplayerOwner(player.getGameProfile()))
			return; // in LAN, don't sync to self

		// build packet
		ConstructSyncPacket packet = new ConstructSyncPacket(this.constructs);
		PortalCubedPackets.sendToClient(player, packet);
	}

	public void readFromPacket(ConstructSyncPacket packet) {
		this.reset();
		packet.getConstructs().forEach(this::addConstruct);
	}

	protected static <T> Optional<Construct.Holder> tryParseConstruct(ResourceLocation id, DynamicOps<T> ops, T data) {
		Construct construct = Construct.CODEC.parse(ops, data).get().map(
				Function.identity(),
				partial -> {
					logger.error("Failed to parse construct {}: {}", id, partial.message());
					return null;
				}
		);

		return construct == null ? Optional.empty() : Optional.of(new Construct.Holder(id, construct));
	}

	private void addConstruct(Construct.Holder holder) {
		Construct construct = holder.construct();
		this.constructs.put(holder.id(), construct);
		this.byMaterial.computeIfAbsent(construct.material, $ -> {
			Comparator<Construct> comparator = Comparator.comparing(this.constructs.inverse()::get);
			return new TreeSet<>(comparator);
		}).add(construct);
	}

	private void reset() {
		this.constructs.clear();
		this.byMaterial.clear();
	}

	public static void init() {
		ResourceLoader.get(PackType.SERVER_DATA).registerReloader(INSTANCE);

		// events for syncing to players
		ServerPlayConnectionEvents.JOIN.register(
				(handler, sender, server) -> INSTANCE.syncToPlayer(handler.player)
		);
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register(
				ctx -> ctx.server().getPlayerList().getPlayers().forEach(INSTANCE::syncToPlayer)
		);
	}

	// API

	@Nullable
	public Construct getConstruct(ResourceLocation id) {
		return this.constructs.get(id);
	}

	public Optional<Construct> maybeGetConstruct(ResourceLocation id) {
		return Optional.ofNullable(this.getConstruct(id));
	}

	public SortedSet<Construct> getConstructsForMaterial(Item material) {
		return this.byMaterial.getOrDefault(material, emptySet);
	}

	public Set<Item> getMaterials() {
		return this.byMaterial.keySet();
	}
}
