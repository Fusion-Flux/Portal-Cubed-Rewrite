package io.github.fusionflux.portalcubed.framework.model.dynamictexture;

import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.mixin.client.BlockModelAccessor;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.AfterBake;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.SimpleBakedModel;

public class DynamicTextureWrapper implements AfterBake {
	public static final String REFERENCE_MARKER = String.format("%s:dynamic", PortalCubed.ID);

	@Override
	@Nullable
	public BakedModel modifyModelAfterBake(BakedModel model, Context context) {
		if (model instanceof SimpleBakedModel && context.sourceModel() instanceof BlockModel sourceModel) {
			for (Map.Entry<String, Either<Material, String>> entry : ((BlockModelAccessor) sourceModel).getTextureMap().entrySet()) {
				Optional<String> reference = entry.getValue().right();
				if (reference.map(REFERENCE_MARKER::equals).orElse(false))
					return new DynamicTextureBakedModel(model);
			}
		}
		return model;
	}
}
