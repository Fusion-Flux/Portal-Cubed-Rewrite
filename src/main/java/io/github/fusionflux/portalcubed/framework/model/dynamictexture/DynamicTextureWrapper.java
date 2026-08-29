package io.github.fusionflux.portalcubed.framework.model.dynamictexture;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.AfterBakeBlock;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

public class DynamicTextureWrapper implements AfterBakeBlock {
	public static final String REFERENCE_MARKER = String.format("%s:dynamic", PortalCubed.ID);

	@Override
	public BlockStateModel modifyModelAfterBake(BlockStateModel model, Context context) {
		if (context.sourceModel() instanceof BlockStateModel.SimpleCachedUnbakedRoot sourceModel) {
			// TODO: Can't test this until the project compiles - Max
			sourceModel.resolveDependencies(System.out::println);
//			for (Map.Entry<String, TextureSlots.SlotContents> entry : ((BlockModelAccessor) sourceModel).getTextureSlots().values().entrySet()) {
//				if (entry.getValue() instanceof TextureSlots.Reference(String reference) && reference.equals(REFERENCE_MARKER))
//					return new DynamicTextureBakedModel(model);
//			}
		}
		return model;
	}
}
