package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.fizzler.DisintegrateEffect;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

public class PortalCubedDisintegrateEffects {
	public static final DisintegrateEffect DAMAGE = register(
			"damage",
			new DisintegrateEffect(
					entity -> entity instanceof LivingEntity,
					(world, entity) -> entity.hurtServer(world, PortalCubedDamageSources.disintegration(world, entity), Float.MAX_VALUE)
			)
	);
	public static final DisintegrateEffect REMOVE = register(
			"remove",
			new DisintegrateEffect(
					entity -> !(entity instanceof ServerPlayer),
					(world, entity) -> entity.remove(Entity.RemovalReason.KILLED)
			)
	);
	private static final DisintegrateEffect DESTROY_ITEM = register(
			"destroy_item",
			new DisintegrateEffect(
					entity -> entity instanceof ItemEntity,
					(world, entity) -> ((ItemEntity) entity).getItem().onDestroyed((ItemEntity) entity)
			)
	);

	private static DisintegrateEffect register(String name, DisintegrateEffect disintegrateEffect) {
		ResourceLocation id = PortalCubed.id(name);
		return Registry.register(PortalCubedRegistries.DISINTEGRATE_EFFECT, id, disintegrateEffect);
	}

	public static void init() {
	}
}
