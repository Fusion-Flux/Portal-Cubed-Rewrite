package io.github.fusionflux.portalcubed.framework.model;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import net.minecraft.server.packs.resources.ResourceManager;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleSynchronousResourceReloader;

public enum SpriteFinderCache implements SimpleSynchronousResourceReloader {
	INSTANCE;

	public static final ResourceLocation ID = PortalCubed.id("sprite_finder_cache");

	public static void register() {
		ResourceLoader resourceLoader = ResourceLoader.get(PackType.CLIENT_RESOURCES);
		resourceLoader.addReloaderOrdering(ResourceReloaderKeys.Client.MODELS, ID);
		resourceLoader.registerReloader(INSTANCE);
	}

	private SpriteFinder blockAtlasSpriteFinder;

	public SpriteFinder forBlockAtlas() {
		return this.blockAtlasSpriteFinder;
	}

	@Override
	@NotNull
	public ResourceLocation getQuiltId() {
		return ID;
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		ModelManager modelManager = Minecraft.getInstance().getModelManager();
		this.blockAtlasSpriteFinder = SpriteFinder.get(modelManager.getAtlas(TextureAtlas.LOCATION_BLOCKS));
	}
}
