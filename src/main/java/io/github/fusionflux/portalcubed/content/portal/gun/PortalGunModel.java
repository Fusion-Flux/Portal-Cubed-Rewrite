package io.github.fusionflux.portalcubed.content.portal.gun;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PortalGunModel implements ItemModel {
	public static final ItemModel INSTANCE = new PortalGunModel();

	@Override
	public void update(
			ItemStackRenderState renderState,
			ItemStack stack,
			ItemModelResolver itemModelResolver,
			ItemDisplayContext displayContext,
			@Nullable ClientLevel level,
			@Nullable LivingEntity entity,
			int seed
	) {
		PortalGunSettings portalGun = PortalGunItem.getGunSettings(stack);
		Optional<ResourceLocation> skinModel = Optional.ofNullable(portalGun)
				.map(PortalGunSettings::skin)
				.flatMap(skin -> Optional.ofNullable(skin.itemModel()));
		if (skinModel.isPresent()) {
			ModelManager modelManager = Minecraft.getInstance().getModelManager();
			ItemModel itemModel = modelManager.getItemModel(skinModel.get());
			itemModel.update(renderState, stack, itemModelResolver, displayContext, level, entity, seed);
		}
	}

	public record Unbaked() implements ItemModel.Unbaked {
		public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

		@Override
		public MapCodec<Unbaked> type() {
			return CODEC;
		}

		@Override
		public ItemModel bake(BakingContext context) {
			return PortalGunModel.INSTANCE;
		}

		@Override
		public void resolveDependencies(Resolver resolver) {

		}
	}
}
