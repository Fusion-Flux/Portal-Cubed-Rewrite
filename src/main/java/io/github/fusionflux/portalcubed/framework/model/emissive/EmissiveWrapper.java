package io.github.fusionflux.portalcubed.framework.model.emissive;

import java.util.Collection;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.AfterBake;
import net.fabricmc.fabric.api.renderer.v1.model.WrapperBakedModel;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.resources.ResourceLocation;

public record EmissiveWrapper(EmissiveData data) implements AfterBake {
	@Override
	@Nullable
	public BakedModel modifyModelAfterBake(BakedModel model, Context context) {
		Collection<ResourceLocation> textures = data.getEmissiveTexturesForModel(context.id());
		if (!textures.isEmpty()) {
			SimpleBakedModel simple = getSimpleBakedModel(model);
			if (simple != null) {
				return new EmissiveBakedModel(simple, textures);
			}
		}
		return model;
	}

	private SimpleBakedModel getSimpleBakedModel(BakedModel model) {
		if (WrapperBakedModel.unwrap(model) instanceof SimpleBakedModel simple) {
			return simple;
		}
		return null;
	}
}
