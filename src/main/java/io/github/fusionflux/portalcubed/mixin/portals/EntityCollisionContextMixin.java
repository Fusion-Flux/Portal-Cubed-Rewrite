package io.github.fusionflux.portalcubed.mixin.portals;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.collision.PortalAwareCollisionContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

@Mixin(EntityCollisionContext.class)
public abstract class EntityCollisionContextMixin implements PortalAwareCollisionContext {
	@Shadow
	@Nullable
	public abstract Entity getEntity();

	@Nullable
	@Override
	public Vec3 pc$pos() {
		Entity entity = this.getEntity();
		if (PortalTeleportHandler.ignoresPortalModifiedCollision(entity))
			return null;

		return PortalTeleportHandler.centerOf(entity);
	}
}
