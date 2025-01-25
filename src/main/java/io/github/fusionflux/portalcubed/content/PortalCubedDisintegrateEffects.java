package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.fizzler.DisintegrateEffect;
import io.github.fusionflux.portalcubed.mixin.disintegration.ArmorStandAccessor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public class PortalCubedDisintegrateEffects {
	public static final DisintegrateEffect DAMAGE = register(
			"damage",
			new DisintegrateEffect(
					entity -> entity instanceof LivingEntity && !(entity instanceof ArmorStand),
					(world, entity) -> entity.hurtServer(world, PortalCubedDamageSources.disintegration(world, entity), Float.MAX_VALUE)
			)
	);
	private static final DisintegrateEffect BREAK_ARMOR_STAND = register(
			"break_armor_stand",
			new DisintegrateEffect(
					entity -> entity instanceof ArmorStand,
					(world, entity) -> ((ArmorStandAccessor) entity).callBrokenByAnything(world, PortalCubedDamageSources.disintegration(world, entity))
			)
	);
	private static final DisintegrateEffect DESTROY_ITEM = register(
			"destroy_item",
			new DisintegrateEffect(
					entity -> entity instanceof ItemEntity,
					(world, entity) -> ((ItemEntity) entity).getItem().onDestroyed((ItemEntity) entity)
			)
	);
	public static final DisintegrateEffect REMOVE = register(
			"remove",
			new DisintegrateEffect(
					entity -> !(entity instanceof Player),
					(world, entity) -> entity.remove(Entity.RemovalReason.KILLED)
			)
	);

	private static DisintegrateEffect register(String name, DisintegrateEffect disintegrateEffect) {
		ResourceLocation id = PortalCubed.id(name);
		return Registry.register(PortalCubedRegistries.DISINTEGRATE_EFFECT, id, disintegrateEffect);
	}

	public static void init() {
	}
}
