package io.github.fusionflux.portalcubed.content.portal.renderer;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;

public class PortalTextureManager extends TextureAtlasHolder implements IdentifiableResourceReloadListener {
	public static final ResourceLocation ID = PortalCubed.id("portals");
	public static final ResourceLocation ATLAS_LOCATION = PortalCubed.id("textures/atlas/portals.png");

	public static final PortalTextureManager INSTANCE = new PortalTextureManager();

	public PortalTextureManager() {
		super(Minecraft.getInstance().getTextureManager(), ATLAS_LOCATION, ID);
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	public TextureAtlasSprite getSprite(ResourceLocation location) {
		return super.getSprite(location);
	}
}
