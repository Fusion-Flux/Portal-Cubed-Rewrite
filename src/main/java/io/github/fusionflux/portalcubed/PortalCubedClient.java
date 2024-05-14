package io.github.fusionflux.portalcubed;

import com.terraformersmc.terraform.boat.api.client.TerraformBoatClientHelper;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.PortalCubedFluids;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedKeyMappings;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.cannon.ConstructPreviewRenderer;
import io.github.fusionflux.portalcubed.content.misc.LemonadeItem;
import io.github.fusionflux.portalcubed.content.portal.PortalRenderer;
import io.github.fusionflux.portalcubed.content.prop.PropModels;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import io.github.fusionflux.portalcubed.framework.model.PortalCubedModelLoadingPlugin;
import io.github.fusionflux.portalcubed.framework.model.emissive.EmissiveLoader;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import org.intellij.lang.annotations.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.entity.event.api.client.ClientEntityTickCallback;

public class PortalCubedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		PortalRenderer.init();
		ConstructPreviewRenderer.init();
		PortalCubedKeyMappings.init();

		final ResourceLocation toxicGooStillSpriteId =  PortalCubed.id("block/toxic_goo_still");
		final ResourceLocation toxicGooFlowSpriteId =  PortalCubed.id("block/toxic_goo_flow");
			FluidRenderHandlerRegistry.INSTANCE.register(PortalCubedFluids.STILL_GOO, PortalCubedFluids.FLOWING_GOO, new SimpleFluidRenderHandler(toxicGooStillSpriteId, toxicGooFlowSpriteId));

		ItemProperties.register(PortalCubedItems.LEMONADE, PortalCubed.id("armed"), (stack, level, entity, i) -> LemonadeItem.isArmed(stack) ? 1 : 0);

		TerraformBoatClientHelper.registerModelLayers(PortalCubedEntities.LEMON_BOAT.location(), false);
		PropModels.register();
		PreparableModelLoadingPlugin.register(EmissiveLoader.INSTANCE, PortalCubedModelLoadingPlugin.INSTANCE);

		ClientEntityTickCallback.EVENT.register((entity, isPassengerTick) -> {
			if (entity instanceof Player player) {
				boolean holdingPortalGun = player.getMainHandItem().is(PortalCubedItems.PORTAL_GUN);

				var soundManager = Minecraft.getInstance().getSoundManager();
				int grabSoundTimer = player.pc$grabSoundTimer();
				var holdLoopSound = (FollowingSoundInstance) player.pc$holdLoopSound();
				var grabSound = (FollowingSoundInstance) player.pc$grabSound();

				if (grabSound != null) {
					grabSoundTimer--;
					player.pc$grabSoundTimer(grabSoundTimer);

					if (!holdingPortalGun) {
						grabSound.forceStop();
						grabSound = null;
						player.pc$grabSound(grabSound);
					} else if (grabSoundTimer <= 0) {
						grabSound = null;
						player.pc$grabSound(grabSound);

						holdLoopSound = PortalCubedSounds.createPortalGunHoldLoop(player);
						soundManager.play(holdLoopSound);
						player.pc$holdLoopSound(holdLoopSound);
					}
				}

				if (holdLoopSound != null && !holdingPortalGun) {
					holdLoopSound.forceStop();
					player.pc$holdLoopSound(null);
				} else if (holdingPortalGun && (holdLoopSound == null && grabSound == null) && player.getHeldEntity() != null) {
					holdLoopSound = PortalCubedSounds.createPortalGunHoldLoop(player);
					soundManager.play(holdLoopSound);
					player.pc$holdLoopSound(holdLoopSound);
				}
			}
		});
	}
}
