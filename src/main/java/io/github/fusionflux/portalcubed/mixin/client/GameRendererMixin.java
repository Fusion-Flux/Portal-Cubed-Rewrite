package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	// TODO: Disintegration - Max
//	@ModifyExpressionValue(method = "getFov", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F"))
//	private float dontChangeFovIfDisintegrated(float original, @Local(argsOnly = true) Camera camera) {
//		Entity entity = camera.entity();
//		return entity != null && entity.pc$disintegrating() ? 0 : original;
//	}
//
//	@ModifyExpressionValue(method = "bobHurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;hurtTime:I"))
//	private int dontHurtBobViewIfDisintegrated(int original, @Local LivingEntity livingEntity) {
//		return livingEntity.pc$disintegrating() ? 0 : original;
//	}
//
//	@ModifyExpressionValue(method = "bobHurt", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F"))
//	private float dontDeathBobViewIfDisintegrated(float original, @Local LivingEntity livingEntity) {
//		return livingEntity.pc$disintegrating() ? 0 : original;
//	}
}
