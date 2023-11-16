package io.github.fusionflux.portalcubed.mixin;

import io.github.fusionflux.portalcubed.framework.extension.EntityCollisionContextExt;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityCollisionContext.class)
public class EntityCollisionContextMixin implements EntityCollisionContextExt {
	@Unique
	private boolean doPortalCollision = true;

	@Override
	public boolean pc$doPortalCollision() {
		return doPortalCollision;
	}

	@Override
	public void pc$setPortalCollision(boolean value) {
		this.doPortalCollision = value;
	}
}
