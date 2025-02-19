package io.github.fusionflux.portalcubed.mixin.portals.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.content.portal.sync.EntityState;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Inject(
			method = "renderEntity",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/renderer/LevelRenderer;entityRenderDispatcher:Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;",
					ordinal = 0
			)
	)
	private void applyMidTeleportSubTickMotion(Entity entity, double camX, double camY, double camZ, float partialTicks,
											   PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci,
											   @Local(ordinal = 3) LocalDoubleRef x, @Local(ordinal = 4) LocalDoubleRef y,
											   @Local(ordinal = 5) LocalDoubleRef z) {
		EntityState override = entity.getTeleportProgressTracker().getEntityStateOverride(partialTicks);
		if (override != null) {
			x.set(override.pos().x);
			y.set(override.pos().y);
			z.set(override.pos().z);
		}
	}
}
