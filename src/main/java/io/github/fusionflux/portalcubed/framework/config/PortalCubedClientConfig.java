package io.github.fusionflux.portalcubed.framework.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.graphics.render.PortalRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.ExtraCodecs;

public record PortalCubedClientConfig(int portalRenderingLevels) {
	public static final Codec<PortalCubedClientConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
			ExtraCodecs.intRange(0, PortalRenderer.MAX_LEVELS).fieldOf("portal_rendering_levels").forGetter(PortalCubedClientConfig::portalRenderingLevels)
	).apply(i, PortalCubedClientConfig::new));

	public static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("portalcubed.json");

	private static final Logger logger = LogUtils.getLogger();
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final PortalCubedClientConfig defaults = new PortalCubedClientConfig(4);
	private static final List<Consumer<PortalCubedClientConfig>> changeCallbacks = new ArrayList<>();

	private static PortalCubedClientConfig instance;

	public static PortalCubedClientConfig get() {
		if (instance == null) {
			try {
				instance = load();
			} catch (IOException | JsonParseException e) {
				logger.error("Failed to read Portal Cubed config, using defaults", e);
				instance = defaults;
			}
		}

		return instance;
	}

	public static void set(PortalCubedClientConfig config) {
		try {
			save(config);
		} catch (IOException e) {
			logger.error("Failed to save Portal Cubed config", e);
		}

		instance = config;
		changeCallbacks.forEach(callback -> callback.accept(config));
	}

	public static void reset() {
		set(defaults);
	}

	public static void onChange(Consumer<PortalCubedClientConfig> callback) {
		changeCallbacks.add(callback);
	}

	private static PortalCubedClientConfig load() throws IOException, JsonParseException {
		if (!Files.exists(PATH)) {
			// no config file yet, write the defaults
			save(defaults);
		}

		try (BufferedReader reader = Files.newBufferedReader(PATH)) {
			JsonElement json = JsonParser.parseReader(reader);
			DataResult<Pair<PortalCubedClientConfig, JsonElement>> result = CODEC.decode(JsonOps.INSTANCE, json);
			if (result instanceof DataResult.Error<?> error) {
				throw new JsonParseException("Failed to parse config: " + error.message());
			}

			return result.getOrThrow().getFirst();
		}
	}

	private static void save(PortalCubedClientConfig config) throws IOException {
		JsonElement json = CODEC.encodeStart(JsonOps.INSTANCE, config).getOrThrow();
		String jsonString = gson.toJson(json);
		Files.deleteIfExists(PATH);
		Files.writeString(PATH, jsonString, StandardOpenOption.CREATE);
	}
}
