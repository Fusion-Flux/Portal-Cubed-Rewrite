package io.github.fusionflux.portalcubed.content.fizzler;

import java.util.function.Predicate;

import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public record DisintegrateEffect(Predicate<Entity> entitySelector, EntityModifier entityModifier) {
	public static void applyAll(ServerLevel world, Entity entity) {
		PortalCubedRegistries.DISINTEGRATE_EFFECT
				.listElements()
				.map(Holder::value)
				.filter(disintegrateEffect -> disintegrateEffect.entitySelector.test(entity))
				.forEach(disintegrateEffect -> disintegrateEffect.entityModifier.apply(world, entity));
	}

	@FunctionalInterface
	public interface EntityModifier {
		void apply(ServerLevel world, Entity entity);
	}
}
