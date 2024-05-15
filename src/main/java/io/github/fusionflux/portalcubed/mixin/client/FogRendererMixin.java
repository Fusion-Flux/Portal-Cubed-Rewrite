package io.github.fusionflux.portalcubed.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.fusionflux.portalcubed.content.goo.GooFluid;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
	@Shadow private static float fogRed;
	@Shadow private static float fogGreen;
	@Shadow private static float fogBlue;
	@Shadow private static long biomeChangedTime;

	@Inject(method = "setupColor", at = @At("HEAD"), cancellable = true)
	private static void setupToxicGooFogColor(Camera camera, float tickDelta, ClientLevel world, int viewDistance, float skyDarkness, CallbackInfo ci) {
		BlockPos cameraBlockPos = camera.getBlockPosition();
		FluidState state = world.getFluidState(cameraBlockPos);
		if (state.getType() instanceof GooFluid) {
			if (camera.getPosition().y < (cameraBlockPos.getY() + state.getHeight(world, cameraBlockPos))) {
				fogRed = 99 / 255f;
				fogGreen = 29 / 255f;
				fogBlue = 1 / 255f;
				biomeChangedTime = -1L;

				RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0f);
				ci.cancel();
			}
		}
	}

	@Inject(method = "setupFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogShape(Lcom/mojang/blaze3d/shaders/FogShape;)V"))
	private static void setupToxicGooFog(Camera camera, FogRenderer.FogMode fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
		Entity cameraEntity = camera.getEntity();
		Level world = cameraEntity.level();
		BlockPos cameraBlockPos = camera.getBlockPosition();
		FluidState state = world.getFluidState(cameraBlockPos);
		if (state.getType() instanceof GooFluid) {
			if (camera.getPosition().y < (cameraBlockPos.getY() + state.getHeight(world, cameraBlockPos))) {
				if (camera.getEntity().isSpectator()) {
					RenderSystem.setShaderFogStart(-8f);
					RenderSystem.setShaderFogEnd(viewDistance * 0.5f);
				} else {
					RenderSystem.setShaderFogStart(0f);
					RenderSystem.setShaderFogEnd(3f);
				}
			}
		}
	}
}
