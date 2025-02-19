package io.github.fusionflux.portalcubed.mixin.portals.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.sync.EntityState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

/**
 * This is pretty much only handling shadows.
 * @see LevelRendererMixin
 */
@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
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
