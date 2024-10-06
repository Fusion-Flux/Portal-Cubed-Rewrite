package io.github.fusionflux.portalcubed.framework.model.emissive;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.serialization.JsonOps;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin.DataLoader;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

public enum EmissiveLoader implements DataLoader<EmissiveData> {
	INSTANCE;

	public static final ResourceLocation EMISSIVES_JSON_LOCATION = PortalCubed.id("emissives.json");

	@Override
	public CompletableFuture<EmissiveData> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> manager.getResource(EMISSIVES_JSON_LOCATION).map(resource -> {
			try (BufferedReader reader = resource.openAsReader()) {
				return Util.getOrThrow(
						EmissiveData.CODEC.parse(JsonOps.INSTANCE, GsonHelper.parse(reader)),
						RuntimeException::new
				);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).orElseThrow(), executor);
	}
}
