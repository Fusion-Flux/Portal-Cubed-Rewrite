package io.github.fusionflux.portalcubed.framework.signage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.JsonElement;

import com.mojang.serialization.JsonOps;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;

import net.minecraft.util.profiling.ProfilerFiller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class SignageManager extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloader {
	public static final ResourceLocation ID = PortalCubed.id("signage");

	private static final Logger logger = LoggerFactory.getLogger(ConstructManager.class);
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static final SignageManager INSTANCE = new SignageManager();

	private final SortedMap<ResourceLocation, Signage.Holder> entries = new TreeMap<>();
	private final EnumMap<Signage.Size, Signage.Holder> blank = new EnumMap<>(Signage.Size.class);

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
					.map(signage -> this.entries.put(id, new Signage.Holder(id, signage)));
		}
		this.findBlank();
	}

	public void readFromPacket(SignageSyncPacket packet) {
		this.reset();
		this.findBlank();
	}

	private void reset() {
		this.entries.forEach(($, holder) -> holder.unbindValue());
		this.entries.clear();
		this.blank.clear();
	}

	private void findBlank() {
		for (Signage.Size size : Signage.Size.values()) {
			ResourceLocation id = PortalCubed.id(String.format("blank_%s", size.name));
			Signage.Holder holder = this.entries.get(id);
			if (holder != null)
				this.blank.put(size, holder);
		}
	}

	public static void init() {
		ResourceLoader.get(PackType.SERVER_DATA).registerReloader(INSTANCE);
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

	public Collection<Signage> allOfSize(Signage.Size size) {
		return this.entries.values().stream()
				.map(Signage.Holder::value)
				.filter(Objects::nonNull)
				.filter(signage -> signage.size() == size)
				.toList();
	}
}
