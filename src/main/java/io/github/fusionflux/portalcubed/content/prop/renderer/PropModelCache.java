package io.github.fusionflux.portalcubed.content.prop.renderer;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.framework.util.SimpleSynchronousReloadListener;
import io.github.fusionflux.portalcubed.mixin.client.ItemStackRenderStateAccessor;
import io.github.fusionflux.portalcubed.mixin.client.LayerRenderStateAccessor;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

// TODO: FRAPI is a rube goldberg machine now. HELP ME - Max
public enum PropModelCache implements SimpleSynchronousReloadListener {
	INSTANCE;

	public static final Identifier ID = PortalCubed.id("prop_models");
	public static final Collection<Identifier> DEPENDENCIES = List.of(ResourceReloadListenerKeys.MODELS);

	private final ItemStackRenderState scratchRenderState = new ItemStackRenderState();
	private final EnumMap<PropType, MeshAndTransform[][]> cache = new EnumMap<>(PropType.class);

	public MeshAndTransform[] get(PropRenderState renderState) {
		MeshAndTransform[][] variants = this.cache.get(renderState.type);
		return variants[Math.min(renderState.variant, variants.length)];
	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}

	@Override
	public Collection<Identifier> getFabricDependencies() {
		return DEPENDENCIES;
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		this.cache.clear();
		ItemModelResolver modelResolver = Minecraft.getInstance().getItemModelResolver();
		for (PropType type : PropType.values()) {
			Item item = type.item();
			ItemStack stack = item.getDefaultInstance();
			MeshAndTransform[][] variants = this.cache.compute(
					type,
					($, v) -> v == null ? new MeshAndTransform[type.variants.length][] : Util.make(v, arr -> Arrays.fill(arr, null))
			);
			for (int variant : type.variants) {
				stack.set(PortalCubedDataComponents.PROP_VARIANT, variant);
				modelResolver.updateForTopItem(this.scratchRenderState, stack, ItemDisplayContext.GROUND, null, null, 42);

				ItemStackRenderState.LayerRenderState[] layers = ((ItemStackRenderStateAccessor) this.scratchRenderState).getLayers();
				MeshAndTransform[] meshTransformPairs = new MeshAndTransform[((ItemStackRenderStateAccessor) this.scratchRenderState).getActiveLayerCount()];
				for (int i = 0; i < meshTransformPairs.length; i++) {
					ItemStackRenderState.LayerRenderState layer = layers[i];
					meshTransformPairs[i] = new MeshAndTransform(
							((LayerRenderStateAccessor) layer).getQuads(),
							((LayerRenderStateAccessor) layer).getItemTransform(),
							new Matrix4f(((LayerRenderStateAccessor) layer).getLocalTransform())
					);
				}

				variants[variant] = meshTransformPairs;
			}
		}
	}

	public record MeshAndTransform(ItemQuads quads, ItemTransform transform, Matrix4fc localTransform) {
		public void applyTransform(PoseStack.Pose pose) {
			this.transform.apply(false, pose);
			pose.mulPose(this.localTransform);
			pose.translate(-.5f, -.5f, -.5f);
		}
	}
}
