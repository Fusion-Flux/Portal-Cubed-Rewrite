package io.github.fusionflux.portalcubed.mixin.client;

import java.util.Objects;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.framework.extension.BigShapeBlock;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", shift = At.Shift.BEFORE))
	private void bigShapePick(float tickDelta, CallbackInfo ci) {
		LocalPlayer player = Objects.requireNonNull(this.minecraft.player);
		double range = player.blockInteractionRange();
		BigShapeBlock.pick(range, tickDelta);
	}

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
