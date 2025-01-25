package io.github.fusionflux.portalcubed.content.fizzler;

import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface FizzleBehaviour {
	boolean fizzle(Entity entity);
}
