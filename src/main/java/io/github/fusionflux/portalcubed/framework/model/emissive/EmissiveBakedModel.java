package io.github.fusionflux.portalcubed.framework.model.emissive;

import java.util.Collection;

import io.github.fusionflux.portalcubed.framework.model.RenderMaterials;
import io.github.fusionflux.portalcubed.framework.model.SpriteFinderCache;
import io.github.fusionflux.portalcubed.framework.model.TransformingBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

public class EmissiveBakedModel extends TransformingBakedModel {
	public EmissiveBakedModel(BakedModel wrapped, Collection<EmissiveTexturePredicate> predicates) {
		super(quad -> {
			ResourceLocation texture = SpriteFinderCache.INSTANCE
					.forBlockAtlas()
					.find(quad)
					.contents().name();
			for (EmissiveTexturePredicate predicate : predicates) {
				if (predicate.test(texture)) {
					quad.material(RenderMaterials.makeEmissive(quad.material()));
					break;
				}
			}
			return true;
		});
		this.wrapped = wrapped;
	}
}
