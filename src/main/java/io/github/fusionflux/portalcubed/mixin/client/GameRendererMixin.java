package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@ModifyExpressionValue(method = "getFov", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F"))
	private float dontChangeFovIfDisintegrated(float original, @Local(argsOnly = true) Camera camera) {
		return camera.getEntity().pc$disintegrating() ? 0 : original;
	}

	@ModifyExpressionValue(method = "bobHurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;hurtTime:I"))
	private int dontHurtBobViewIfDisintegrated(int original, @Local LivingEntity livingEntity) {
		return livingEntity.pc$disintegrating() ? 0 : original;
	}

	@ModifyExpressionValue(method = "bobHurt", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F"))
	private float dontDeathBobViewIfDisintegrated(float original, @Local LivingEntity livingEntity) {
		return livingEntity.pc$disintegrating() ? 0 : original;
	}
}
