package io.github.fusionflux.portalcubed.framework.model.emissive;

import java.util.Collection;

import io.github.fusionflux.portalcubed.framework.model.RenderMaterials;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.AfterBake;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

public record EmissiveWrapper(EmissiveData data) implements AfterBake {
	@Override
	@Nullable
	public BakedModel modifyModelAfterBake(BakedModel model, Context context) {
		if (RenderMaterials.ARE_SUPPORTED) {
			Collection<ResourceLocation> textures = data.getEmissiveTexturesForModel(context.id());
			if (!textures.isEmpty())
				return new EmissiveBakedModel(model, textures);
		}
		return model;
	}
}
