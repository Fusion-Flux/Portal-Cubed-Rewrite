package io.github.fusionflux.portalcubed.content.portal.graphics.render;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.Identifier;

public final class PortalTextureManager extends TextureAtlasHolder implements IdentifiableResourceReloadListener {
	public static final Identifier ID = PortalCubed.id("portals");
	public static final Identifier ATLAS_LOCATION = PortalCubed.id("textures/atlas/portals.png");

	public static final PortalTextureManager INSTANCE = new PortalTextureManager();

	private PortalTextureManager() {
		super(Minecraft.getInstance().getTextureManager(), ATLAS_LOCATION, ID);
	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}

	@Override
	public TextureAtlasSprite getSprite(Identifier location) {
		return super.getSprite(location);
	}
}
