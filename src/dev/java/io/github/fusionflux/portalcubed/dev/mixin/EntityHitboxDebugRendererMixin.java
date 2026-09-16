package io.github.fusionflux.portalcubed.dev.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.framework.util.Colors;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(EntityHitboxDebugRenderer.class)
public final class EntityHitboxDebugRendererMixin {
	// for some reason mixin can't see the names of these locals??? need to use ordinals instead
	@ModifyVariable(method = "showHitboxes", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float showMeTheActualPositionPlease(float partialTicks, @Local(argsOnly = true, ordinal = 0) boolean isServerEntity) {
		// what's the point if you're just going to interpolate it
		return isServerEntity ? 1 : partialTicks;
	}

	// also can't use an expression here. this class might be haunted
	@WrapOperation(
			method = "emitGizmos",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/debug/EntityHitboxDebugRenderer;showHitboxes(Lnet/minecraft/world/entity/Entity;FZ)V",
					ordinal = 0
			)
	)
	private void alsoEmitTrueClientPosition(EntityHitboxDebugRenderer self, Entity entity, float partialTicks, boolean isServerEntity, Operation<Void> original) {
		original.call(self, entity, partialTicks, isServerEntity);
		Gizmos.cuboid(scale(entity.getBoundingBox(), 0.5), GizmoStyle.stroke(Colors.YELLOW));
	}

	@Inject(method = "showHitboxes", at = @At("TAIL"))
	private void emitOldBounds(Entity entity, float partialTicks, boolean isServerEntity, CallbackInfo ci) {
		Vec3 pos = entity.position();
		Vec3 oldPos = entity.oldPosition();
		Vec3 offset = pos.vectorTo(oldPos);
		double scale = isServerEntity ? 0.9 : 0.4;
		AABB bounds = scale(entity.getBoundingBox().move(offset), scale);
		Gizmos.cuboid(bounds, GizmoStyle.stroke(isServerEntity ? Colors.BLUE : Colors.RED));
	}

	@Unique
	private static AABB scale(AABB bounds, double scale) {
		return AABB.ofSize(bounds.getCenter(), bounds.getXsize() * scale, bounds.getYsize() * scale, bounds.getZsize() * scale);
	}
}
