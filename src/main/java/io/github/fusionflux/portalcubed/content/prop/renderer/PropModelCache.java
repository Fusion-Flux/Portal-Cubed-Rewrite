package io.github.fusionflux.portalcubed.content.prop.renderer;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.framework.util.SimpleSynchronousReloadListener;
import io.github.fusionflux.portalcubed.mixin.client.ItemStackRenderStateAccessor;
import io.github.fusionflux.portalcubed.mixin.client.LayerRenderStateAccessor;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public enum PropModelCache implements SimpleSynchronousReloadListener {
	INSTANCE;

	public static final ResourceLocation ID = PortalCubed.id("prop_models");
	public static final Collection<ResourceLocation> DEPENDENCIES = List.of(ResourceReloadListenerKeys.MODELS);

	private final ItemStackRenderState scratchRenderState = new ItemStackRenderState();
	private final EnumMap<PropType, ModelTransformPair[][]> cache = new EnumMap<>(PropType.class);

	public ModelTransformPair[] get(PropRenderState renderState) {
		ModelTransformPair[][] variants = this.cache.get(renderState.type);
		return variants[Math.min(renderState.variant, variants.length)];
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
		this.cache.clear();
		ItemModelResolver modelResolver = Minecraft.getInstance().getItemModelResolver();
		BakedModel missingModel = Minecraft.getInstance().getModelManager().getMissingModel();
		for (PropType type : PropType.values()) {
			Item item = type.item();
			ItemStack stack = item.getDefaultInstance();
			ModelTransformPair[][] variants = this.cache.compute(
					type,
					($, v) -> v == null ? new ModelTransformPair[type.variants.length][] : Util.make(v, arr -> Arrays.fill(arr, null))
			);
			for (int variant : type.variants) {
				stack.set(PortalCubedDataComponents.PROP_VARIANT, variant);
				modelResolver.updateForTopItem(this.scratchRenderState, stack, ItemDisplayContext.GROUND, false, null, null, 42);

				ItemStackRenderState.LayerRenderState[] layers = ((ItemStackRenderStateAccessor) this.scratchRenderState).getLayers();
				ModelTransformPair[] modelTransformPairs = new ModelTransformPair[((ItemStackRenderStateAccessor) this.scratchRenderState).getActiveLayerCount()];
				for (int i = 0; i < modelTransformPairs.length; i++) {
					ItemStackRenderState.LayerRenderState layer = layers[i];
					BakedModel model = ((LayerRenderStateAccessor) layer).getModel();
					ItemTransform transform = ((LayerRenderStateAccessor) layer).callTransform();
					modelTransformPairs[i] = new ModelTransformPair(model == null ? missingModel : model, transform);
				}

				variants[variant] = modelTransformPairs;
			}
		}
	}

	public record ModelTransformPair(BakedModel model, ItemTransform transform) {
		public void applyTransform(PoseStack matrices) {
			this.transform.apply(false, matrices);
			matrices.translate(-.5, -.5, -.5);
		}
	}
}
