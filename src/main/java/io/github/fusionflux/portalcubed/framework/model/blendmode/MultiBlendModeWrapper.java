package io.github.fusionflux.portalcubed.framework.model.blendmode;

import java.util.ArrayList;
import java.util.List;

import io.github.fusionflux.portalcubed.mixin.client.SimpleBakedModelAccessor;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.AfterBake;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;

public class MultiBlendModeWrapper implements AfterBake {
	// recycle list
	private static final List<BakedQuad> quads = new ArrayList<>();

	@Override
	public BakedModel modifyModelAfterBake(BakedModel model, Context context) {
		if (model instanceof SimpleBakedModel simpleModel) {
			if (!quads.isEmpty())
				quads.clear();

			quads.addAll(((SimpleBakedModelAccessor) simpleModel).getUnculledFaces());
			((SimpleBakedModelAccessor) simpleModel).getCulledFaces()
					.values()
					.forEach(quads::addAll);

			for (BakedQuad quad : quads) {
				if (quad.pc$blendMode() != null)
					return new MultiBlendModeBakedModel(simpleModel);
			}
		}
		return model;
	}
}
