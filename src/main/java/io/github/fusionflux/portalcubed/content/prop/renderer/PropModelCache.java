package io.github.fusionflux.portalcubed.content.prop.renderer;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.framework.util.SimpleSynchronousReloadListener;
import io.github.fusionflux.portalcubed.mixin.client.ItemStackRenderStateAccessor;
import io.github.fusionflux.portalcubed.mixin.client.LayerRenderStateAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

public enum PropModelCache implements SimpleSynchronousReloadListener {
	INSTANCE;

	public static final ResourceLocation ID = PortalCubed.id("prop_models");
	public static final Collection<ResourceLocation> DEPENDENCIES = List.of(ResourceReloadListenerKeys.MODELS);

	private final ItemStackRenderState capturingRenderState = new ItemStackRenderState();
	private final EnumMap<PropType, ObjectArrayList<BakedModel>> models = new EnumMap<>(PropType.class);

	public BakedModel get(PropRenderState renderState) {
		ObjectArrayList<BakedModel> variants = this.models.get(renderState.type);
		return variants.get(Math.min(renderState.variant, variants.size()));
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	public Collection<ResourceLocation> getFabricDependencies() {
		return DEPENDENCIES;
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		ItemModelResolver modelResolver = Minecraft.getInstance().getItemModelResolver();
		BakedModel missingModel = Minecraft.getInstance().getModelManager().getMissingModel();
		for (PropType type : PropType.values()) {
			Item item = type.item();
			ItemStack stack = item.getDefaultInstance();
			ObjectArrayList<BakedModel> variants = this.models.compute(type, ($, v) -> v == null ? new ObjectArrayList<>() : Util.make(v, ObjectArrayList::clear));
			for (int variant : type.variants) {
				stack.set(PortalCubedDataComponents.PROP_VARIANT, variant);
				modelResolver.updateForTopItem(this.capturingRenderState, stack, ItemDisplayContext.NONE, false, null, null, 42);
				ItemStackRenderState.LayerRenderState layer = ((ItemStackRenderStateAccessor) this.capturingRenderState).callFirstLayer();
				BakedModel model = ((LayerRenderStateAccessor) layer).getModel();
				variants.add(model == null ? missingModel : model);
			}
		}
		this.capturingRenderState.clear();
	}
}
