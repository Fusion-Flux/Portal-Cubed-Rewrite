package io.github.fusionflux.portalcubed.framework.model.dynamictexture;

import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;

public class DynamicTextureMarkerMaterial extends Material {
	@SuppressWarnings("deprecation")
	public DynamicTextureMarkerMaterial() {
		super(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());
	}
}
