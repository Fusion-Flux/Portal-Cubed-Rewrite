package io.github.fusionflux.portalcubed.mixin.portals.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.fusionflux.portalcubed.content.portal.sync.EntityState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
	@WrapMethod(method = "getBoundingBoxForCulling")
	private AABB getBoundingBoxForCulling(Entity entity, Operation<AABB> original) {
		if (entity.level() instanceof ClientLevel level) {
			TickRateManager tickRateManager = level.tickRateManager();
			float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(entity));
			EntityState override = entity.getTeleportProgressTracker().getEntityStateOverride(tickDelta);
			if (override != null) {
				Vec3 position = entity.position();
				return entity.getBoundingBox()
						.move(-position.x, -position.y, -position.z)
						.move(override.pos());
			}
		}

		return original.call(entity);
	}

	/**
	 * This is pretty much only handling shadows.
	 * @see LevelRendererMixin
	 */
	@ModifyReturnValue(
			method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
			at = @At("RETURN")
	)
	private EntityRenderState applyMidTeleportSubTickMotion(EntityRenderState state, Entity entity, float partialTicks) {
		EntityState override = entity.getTeleportProgressTracker().getEntityStateOverride(partialTicks);
		if (override != null) {
			override.apply(state);
		}
		return state;
	}
}
