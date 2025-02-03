package io.github.fusionflux.portalcubed.framework.model.emissive;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.model.RenderMaterials;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.AfterBake;
import net.minecraft.client.resources.model.BakedModel;

public record EmissiveWrapper(EmissiveData data) implements AfterBake {
	@Override
	@Nullable
	public BakedModel modifyModelAfterBake(BakedModel model, Context context) {
		if (RenderMaterials.ARE_SUPPORTED) {
			Collection<EmissiveTexturePredicate> predicates = this.data.predicatesForModel(context.id());
			if (!predicates.isEmpty())
				return new EmissiveBakedModel(model, predicates);
		}
		return model;
	}
}
