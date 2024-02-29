package io.github.fusionflux.portalcubed.content.prop;

import java.util.EnumMap;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleSynchronousResourceReloader;

import io.github.fusionflux.portalcubed.PortalCubed;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

public class PropModels implements SimpleSynchronousResourceReloader {
	public static void register() {
		var resourceLoader = ResourceLoader.get(PackType.CLIENT_RESOURCES);
		resourceLoader.addReloaderOrdering(ResourceReloaderKeys.Client.MODELS, ID);
		resourceLoader.registerReloader(new PropModels());
	}

	public static BakedModel getModel(PropType type, int variant) {
		var variantModels = MODELS.get(type);
		return variantModels.get(variant % variantModels.size());
	}

	public static final ResourceLocation ID = PortalCubed.id("prop_models");
	public static final EnumMap<PropType, ReferenceArrayList<BakedModel>> MODELS = new EnumMap<>(PropType.class);

	@Override
	public @NotNull ResourceLocation getQuiltId() {
		return ID;
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		var modelManager = Minecraft.getInstance().getModelManager();
		for (var propType : PropType.values()) {
			var item = propType.item();
			var stack = new ItemStack(item);
			var model = modelManager.getModel(new ModelResourceLocation(BuiltInRegistries.ITEM.getKey(item), "inventory"));
			var variantModels = new ReferenceArrayList<BakedModel>();
			for (int variant : propType.variants) {
				stack.getOrCreateTag().putInt("CustomModelData", variant);
				var variantModel = model.getOverrides().resolve(model, stack, null, null, 42);
				variantModels.add(variantModel);
			}
			MODELS.put(propType, variantModels);
		}
	}
}
