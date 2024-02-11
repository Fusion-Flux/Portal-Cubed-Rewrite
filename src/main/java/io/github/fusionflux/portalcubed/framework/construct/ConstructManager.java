package io.github.fusionflux.portalcubed.framework.construct;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import net.minecraft.world.item.Item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ConstructManager extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloader {
	public static final ResourceLocation ID = PortalCubed.id("constructs");
	public static final String DIR = "cannon_constructs";

	private static final Logger logger = LoggerFactory.getLogger(ConstructManager.class);
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static ConstructManager INSTANCE = new ConstructManager();

	private final BiMap<ResourceLocation, Construct> constructs = HashBiMap.create();
	private final Map<Item, List<Construct>> byMaterial = new HashMap<>();

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
		cache.forEach((id, json) -> this.tryParseConstruct(id, JsonOps.INSTANCE, json).ifPresent(this::addConstruct));
		this.finishReading();
	}

	public void syncToPlayer(ServerPlayer player) {
		if (player.server.isSingleplayerOwner(player.getGameProfile()))
			return; // in LAN, don't sync to self

		// build packet
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(this.constructs.size());
		this.constructs.forEach((id, construct) -> {
			Construct.CODEC.encodeStart(NbtOps.INSTANCE, construct).get().ifLeft(nbt -> {
				buf.writeResourceLocation(id);
				buf.writeNbt(nbt);
			}).ifRight(partial -> {
				logger.error("Failed to serialize construct {}: {}", id, partial.message());
			});
		});
	}

	public void readFromPacket(FriendlyByteBuf buf) {
		this.reset();

		int size = buf.readVarInt();
		for (int i = 0; i < size; i++) {
			ResourceLocation id = buf.readResourceLocation();
			CompoundTag nbt = buf.readNbt();

			this.tryParseConstruct(id, NbtOps.INSTANCE, nbt).ifPresent(this::addConstruct);
		}

		this.finishReading();
	}

	private void finishReading() {
		this.byMaterial.values().forEach(list -> list.sort(
				// sort alphabetically by id
				Comparator.comparing(this.constructs.inverse()::get)
		));
	}

	private <T> Optional<Pair<ResourceLocation, Construct>> tryParseConstruct(ResourceLocation id, DynamicOps<T> ops, T data) {
		Construct construct = Construct.CODEC.parse(ops, data).get().map(
				Function.identity(),
				partial -> {
					logger.error("Failed to parse construct {}: {}", id, partial.message());
					return null;
				}
		);

		return construct == null ? Optional.empty() : Optional.of(new Pair<>(id, construct));
	}

	private void addConstruct(Pair<ResourceLocation, Construct> pair) {
		ResourceLocation id = pair.getFirst();
		Construct construct = pair.getSecond();

		this.constructs.put(id, construct);
		this.byMaterial.computeIfAbsent(construct.material, $ -> new ArrayList<>()).add(construct);
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

	public List<Construct> getConstructsForMaterial(Item material) {
		return this.byMaterial.getOrDefault(material, List.of());
	}

	public Set<Item> getMaterials() {
		return this.byMaterial.keySet();
	}
}
