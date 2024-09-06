package io.github.fusionflux.portalcubed.framework.signage;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PlayerLookup;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Optionull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class SignageManager extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloader {
	public static final ResourceLocation ID = PortalCubed.id("signage");

	private static final Logger logger = LoggerFactory.getLogger(ConstructManager.class);
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static final SignageManager INSTANCE = new SignageManager();

	private final Object2ObjectOpenHashMap<ResourceLocation, Signage.Holder> entries = new Object2ObjectOpenHashMap<>();
	private final EnumMap<Signage.Size, Signage.Holder> blank = new EnumMap<>(Signage.Size.class);
	private final Set<ResourceLocation> loaded = new HashSet<>();

	private SignageManager() {
		super(gson, ID.getPath());
	}

	@Override
	@NotNull
	public ResourceLocation getQuiltId() {
		return ID;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> cache, ResourceManager manager, ProfilerFiller profiler) {
		this.reset();
		for (Map.Entry<ResourceLocation, JsonElement> entry : cache.entrySet()) {
			ResourceLocation id = entry.getKey();
			Signage.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
					.resultOrPartial(message -> logger.error("Failed to parse signage {}: {}", id, message))
					.ifPresent(signage -> this.loadSignage(id, signage));
		}
		this.update();
	}

	public void syncToPlayer(ServerPlayer player) {
		if (player.server.isSingleplayerOwner(player.getGameProfile()))
			return; // in LAN, don't sync to self

		// build packet
		Map<ResourceLocation, Signage> syncedEntries = this.entries.entrySet()
				.stream()
				.map(e -> Pair.of(e.getKey(), e.getValue().value()))
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		PortalCubedPackets.sendToClient(player, new SignageSyncPacket(syncedEntries));
	}

	public void readFromPacket(SignageSyncPacket packet) {
		this.reset();
		packet.entries().forEach(this::loadSignage);
		this.update();
	}

	private void loadSignage(ResourceLocation id, Signage signage) {
		this.entries.computeIfAbsent(id, $ -> new Signage.Holder(id, null))
				.bindValue(signage);
		this.loaded.add(id);
	}

	private void reset() {
		this.entries.forEach(($, holder) -> holder.bindValue(null));
		this.loaded.clear();
	}

	private void update() {
		for (ResourceLocation id : this.entries.keySet()) {
			if (!this.loaded.contains(id))
				this.entries.remove(id);
		}

		for (Signage.Size size : Signage.Size.values()) {
			ResourceLocation id = PortalCubed.id(String.format("blank_%s", size.name));
			Signage.Holder holder = this.entries.get(id);
			if (holder != null) {
				this.blank.put(size, holder);
			} else {
				this.blank.remove(size);
			}
		}
	}

	public static void init() {
		ResourceLoader.get(PackType.SERVER_DATA).registerReloader(INSTANCE);

		// events for syncing to players
		ServerPlayConnectionEvents.JOIN.register(
				(handler, sender, server) -> INSTANCE.syncToPlayer(handler.player)
		);
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register(ctx -> {
			MinecraftServer server = ctx.server();
			if (server != null)
				PlayerLookup.all(server).forEach(INSTANCE::syncToPlayer);
		});
	}

	// API

	@Nullable
	public Signage.Holder getBlank(Signage.Size size) {
		return this.blank.get(size);
	}

	@Nullable
	public Signage.Holder get(ResourceLocation id) {
		return this.entries.get(id);
	}

	public Collection<Signage.Holder> allOfSize(Signage.Size size) {
		return this.entries.values().stream()
				.filter(holder -> Optionull.map(holder.value(), Signage::size) == size)
				.toList();
	}
}
