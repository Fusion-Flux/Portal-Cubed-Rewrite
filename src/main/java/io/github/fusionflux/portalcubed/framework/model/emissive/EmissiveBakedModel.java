package io.github.fusionflux.portalcubed.framework.model.emissive;

import java.util.Collection;

import io.github.fusionflux.portalcubed.framework.model.RenderMaterials;
import io.github.fusionflux.portalcubed.framework.model.SpriteFinderCache;
import io.github.fusionflux.portalcubed.framework.model.TransformingBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;

import net.minecraft.resources.ResourceLocation;

public class EmissiveBakedModel extends TransformingBakedModel {
	public EmissiveBakedModel(BakedModel wrapped, Collection<ResourceLocation> emissiveTextures) {
		super(quad -> {
			TextureAtlasSprite texture = SpriteFinderCache.INSTANCE
					.forBlockAtlas()
					.find(quad);
			if (emissiveTextures.contains(texture.contents().name()))
				quad.material(RenderMaterials.makeEmissive(quad.material()));
			return true;
		});
		this.wrapped = wrapped;
	}
}
