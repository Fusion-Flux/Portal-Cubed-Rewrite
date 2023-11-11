package io.github.fusionflux.portalcubed.framework.model.rendertype;

import io.github.fusionflux.portalcubed.framework.extension.BlockElementExt;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.BeforeBake;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;

public class MultiRenderTypeWrapper implements BeforeBake {
	@Override
	public UnbakedModel modifyModelBeforeBake(UnbakedModel model, Context context) {
		if (model instanceof BlockModel blockModel) {
			for (BlockElement element : blockModel.getElements()) {
				RenderMaterial material = ((BlockElementExt) element).pc$renderMaterial();
				if (material != null) {
					return new MultiRenderTypeUnbakedModel(blockModel);
				}
			}
		}

		return model;
	}
}
