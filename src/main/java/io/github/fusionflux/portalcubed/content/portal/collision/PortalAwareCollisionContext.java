package io.github.fusionflux.portalcubed.content.portal.collision;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public interface PortalAwareCollisionContext extends CollisionContext {
	@Nullable
	Vec3 pc$pos();
}
