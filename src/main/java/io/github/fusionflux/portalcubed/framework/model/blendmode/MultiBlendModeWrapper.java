package io.github.fusionflux.portalcubed.framework.model.blendmode;

import java.util.ArrayList;
import java.util.List;

import io.github.fusionflux.portalcubed.framework.extension.BakedQuadExt;
import io.github.fusionflux.portalcubed.mixin.client.SimpleBakedModelAccessor;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.AfterBake;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;

public class MultiBlendModeWrapper implements AfterBake {
	@Override
	public BakedModel modifyModelAfterBake(BakedModel model, Context context) {
		if (model instanceof SimpleBakedModel simpleModel) {
			List<BakedQuad> quads = new ArrayList<>();
			((SimpleBakedModelAccessor) simpleModel).getCulledFaces()
					.values()
					.forEach(quads::addAll);
			quads.addAll(((SimpleBakedModelAccessor) simpleModel).getUnculledFaces());
			for (BakedQuad quad : quads) {
				if (((BakedQuadExt) quad).pc$blendMode() != null)
					return new MultiBlendModeBakedModel(simpleModel);
			}
		}
		return model;
	}
}
