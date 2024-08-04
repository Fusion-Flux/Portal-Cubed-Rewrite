package io.github.fusionflux.portalcubed.content.prop;

import java.util.EnumMap;

import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.Util;
import net.minecraft.client.resources.model.ModelManager;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleSynchronousResourceReloader;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

public enum PropModelCache implements SimpleSynchronousResourceReloader {
	INSTANCE;

	public static final ResourceLocation ID = PortalCubed.id("prop_models");

	public static void register() {
		ResourceLoader resourceLoader = ResourceLoader.get(PackType.CLIENT_RESOURCES);
		resourceLoader.addReloaderOrdering(ResourceReloaderKeys.Client.MODELS, ID);
		resourceLoader.registerReloader(INSTANCE);
	}

	private final EnumMap<PropType, ObjectArrayList<BakedModel>> models = new EnumMap<>(PropType.class);

	public BakedModel get(Prop prop) {
		ObjectArrayList<BakedModel> variants = this.models.get(prop.type);
		return variants.get(Math.min(prop.getVariant(), variants.size()));
	}

	@Override
	@NotNull
	public ResourceLocation getQuiltId() {
		return ID;
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		ModelManager modelManager = Minecraft.getInstance().getModelManager();
		for (PropType type : PropType.values()) {
			Item item = type.item();
			ItemStack stack = item.getDefaultInstance();
			BakedModel model = modelManager.getModel(new ModelResourceLocation(BuiltInRegistries.ITEM.getKey(item), "inventory"));
			ObjectArrayList<BakedModel> variants = this.models.compute(type, ($, v) -> v == null ? new ObjectArrayList<>() : Util.make(v, ObjectArrayList::clear));
			for (int variant : type.variants) {
				stack.getOrCreateTag().putInt("CustomModelData", variant);
				BakedModel variantModel = model.getOverrides().resolve(model, stack, null, null, 42);
				variants.add(variantModel == null ? modelManager.getMissingModel() : variantModel);
			}
		}
	}
}
