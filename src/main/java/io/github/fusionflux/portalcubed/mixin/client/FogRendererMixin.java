package io.github.fusionflux.portalcubed.mixin.client;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import io.github.fusionflux.portalcubed.content.goo.GooFluid;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
	@Shadow
	private static long biomeChangedTime;

	@Inject(
			method = "computeFogColor",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/FogRenderer;getPriorityFogFunction(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/FogRenderer$MobEffectFogFunction;"
			)
	)
	private static void setupToxicGooFogColor(
			Camera camera,
			float tickDelta,
			ClientLevel world,
			int renderDistance,
			float darkenWorldAmount,
			CallbackInfoReturnable<Vector4f> cir,
			@Local LocalRef<FogType> fogType,
			@Local(ordinal = 2) LocalFloatRef fogRed,
			@Local(ordinal = 3) LocalFloatRef fogGreen,
			@Local(ordinal = 4) LocalFloatRef fogBlue
	) {
		BlockPos cameraBlockPos = camera.getBlockPosition();
		FluidState state = world.getFluidState(cameraBlockPos);
		if (state.getType() instanceof GooFluid) {
			if (camera.getPosition().y < (cameraBlockPos.getY() + state.getHeight(world, cameraBlockPos))) {
				fogRed.set(99 / 255f);
				fogGreen.set(29 / 255f);
				fogBlue.set(1 / 255f);
				biomeChangedTime = -1L;

				// Lava behaviour in the next few conditionals is the closet to what we want
				fogType.set(FogType.LAVA);
			}
		}
	}

	@Inject(
			method = "setupFog",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/FogRenderer;getPriorityFogFunction(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/FogRenderer$MobEffectFogFunction;"
			),
			cancellable = true
	)
	private static void setupToxicGooFog(
			Camera camera,
			FogRenderer.FogMode fogMode,
			Vector4f fogColor,
			float renderDistance,
			boolean isFoggy,
			float partialTick,
			CallbackInfoReturnable<FogParameters> cir,
			@Local FogRenderer.FogData fogData
	) {
		Entity cameraEntity = camera.getEntity();
		Level world = cameraEntity.level();
		BlockPos cameraBlockPos = camera.getBlockPosition();
		FluidState state = world.getFluidState(cameraBlockPos);
		if (state.getType() instanceof GooFluid) {
			if (camera.getPosition().y < (cameraBlockPos.getY() + state.getHeight(world, cameraBlockPos))) {
				if (cameraEntity.isSpectator()) {
					fogData.start = -8f;
					fogData.end = renderDistance * 0.5f;
				} else {
					fogData.start = 0f;
					fogData.end = 3f;
				}
				cir.setReturnValue(new FogParameters(fogData.start, fogData.end, fogData.shape, fogColor.x, fogColor.y, fogColor.z, fogColor.w));
			}
		}
	}
}
