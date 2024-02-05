package io.github.fusionflux.portalcubed.framework.construct;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import com.mojang.serialization.JsonOps;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import net.minecraft.world.item.Item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

public class ConstructLoader extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloader {
	public static final ResourceLocation ID = PortalCubed.id("constructs");
	public static final String DIR = "cannon_constructs";

	private static final Logger logger = LoggerFactory.getLogger(ConstructLoader.class);
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static ConstructLoader INSTANCE = new ConstructLoader();

	private final BiMap<ResourceLocation, Construct> constructs = HashBiMap.create();
	private final Map<Item, List<Construct>> byMaterial = new HashMap<>();

	private ConstructLoader() {
		super(gson, DIR);
	}

	@Override
	@NotNull
	public ResourceLocation getQuiltId() {
		return ID;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> cache, ResourceManager manager, ProfilerFiller profiler) {
		this.constructs.clear();
		this.byMaterial.clear();

		cache.forEach((id, json) -> {
			Construct parsed = Construct.CODEC.parse(JsonOps.INSTANCE, json).get().map(
					Function.identity(),
					partial -> {
						logger.info("Failed to parse construct {}: {}", id, partial.message());
						return null;
					}
			);

			if (parsed != null) {
				this.constructs.put(id, parsed);
				this.byMaterial.computeIfAbsent(parsed.material, $ -> new ArrayList<>()).add(parsed);
			}
		});

		this.byMaterial.values().forEach(list -> list.sort(
				// sort alphabetically by id
				Comparator.comparing(this.constructs.inverse()::get)
		));
	}

	// todo: client sync

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
