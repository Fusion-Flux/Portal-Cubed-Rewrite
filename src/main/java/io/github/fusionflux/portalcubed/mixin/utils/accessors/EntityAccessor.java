package io.github.fusionflux.portalcubed.mixin.utils.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public interface EntityAccessor {
	@Invoker
	static Vec3 callGetInputVector(Vec3 movementInput, float speed, float yaw) {
		throw new AbstractMethodError();
	}
}
