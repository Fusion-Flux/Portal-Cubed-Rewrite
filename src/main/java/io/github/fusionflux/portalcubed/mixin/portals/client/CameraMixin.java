package io.github.fusionflux.portalcubed.mixin.portals.client;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.sync.EntityState;
import io.github.fusionflux.portalcubed.framework.render.debug.CameraRotator;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	protected abstract void setRotation(float yRot, float xRot);

	@Shadow
	private float yRot;

	@Shadow
	private float xRot;

	@Shadow
	private float eyeHeightOld;

	@Shadow
	private float eyeHeight;

	@Shadow
	@Nullable
	private Entity entity;

	@WrapOperation(
			method = "alignWithEntity",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"
			)
	)
	private void applyMidTeleportSubTickMotion(Camera instance, double x, double y, double z, Operation<Void> original, @Local(argsOnly = true, name = "partialTicks") float partialTicks) {
		EntityState override = EntityState.getOverride(this.entity, partialTicks);
		if (override != null) {
			x = override.pos().x;
			y = override.pos().y + Mth.lerp(partialTicks, this.eyeHeightOld, this.eyeHeight);
			z = override.pos().z;
		}
		original.call(instance, x, y, z);
	}

	// TODO: Portal rendering - Max
//	@ModifyReturnValue(method = "isDetached", at = @At("RETURN"))
//	private boolean detachedIfRenderingPortal(boolean original) {
//		return original || PortalViewRenderer.isPortalView();
//	}

	@ModifyExpressionValue(
			method = "alignWithEntity",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"
			)
	)
	private boolean rotat_e(boolean original) {
		if (CameraRotator.isActive()) {
			this.setRotation((float) (this.yRot + CameraRotator.yRot()), (float) (this.xRot + CameraRotator.xRot()));
		}

		return original;
	}
}
