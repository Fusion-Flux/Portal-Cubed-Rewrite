package io.github.fusionflux.portalcubed.framework.model.dynamictexture;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.mixin.client.BlockModelAccessor;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.AfterBake;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;

public class DynamicTextureWrapper implements AfterBake {
	public static final String REFERENCE_MARKER = String.format("%s:dynamic", PortalCubed.ID);

	@Override
	@Nullable
	public BakedModel modifyModelAfterBake(BakedModel model, Context context) {
		if (model instanceof SimpleBakedModel && context.sourceModel() instanceof BlockModel sourceModel) {
			for (Map.Entry<String, TextureSlots.SlotContents> entry : ((BlockModelAccessor) sourceModel).getTextureSlots().values().entrySet()) {
				if (entry.getValue() instanceof TextureSlots.Reference(String reference) && reference.equals(REFERENCE_MARKER))
					return new DynamicTextureBakedModel(model);
			}
		}
		return model;
	}
}
