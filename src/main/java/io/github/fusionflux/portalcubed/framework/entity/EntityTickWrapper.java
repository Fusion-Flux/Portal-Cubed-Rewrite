package io.github.fusionflux.portalcubed.framework.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.fusionflux.portalcubed.content.fizzler.Disintegration;
import io.github.fusionflux.portalcubed.content.portal.sync.tracker.TeleportTracker;
import net.minecraft.world.entity.Entity;

/// Dedicated class for handling the wrapping of [entity ticks](Entity#tick()).
/// We have several things that must always run either before or after each tick.
public class EntityTickWrapper {
	public static void handle(Entity entity, Operation<Void> original) {
		if (Disintegration.isDisintegrating(entity)) {
			Disintegration.tickDisintegrating(entity);
		} else {
			original.call(entity);
		}

		// this needs to be called after the tick is fully done. Injecting at tail is
		// insufficient, since subclasses can put the super.tick() anywhere they want.
		TeleportTracker.of(entity).ifPresent(TeleportTracker::afterTick);
	}
}
