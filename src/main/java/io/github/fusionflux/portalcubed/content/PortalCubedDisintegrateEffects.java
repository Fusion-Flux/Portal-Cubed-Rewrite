package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.fizzler.DisintegrateEffect;
import io.github.fusionflux.portalcubed.mixin.disintegration.ArmorStandAccessor;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.dimension.end.EnderDragonFight;

public class PortalCubedDisintegrateEffects {
	private static final DisintegrateEffect DAMAGE = register(
			"damage",
			new DisintegrateEffect(
					entity -> entity instanceof LivingEntity && !(entity instanceof ArmorStand),
					(level, entity) -> entity.hurtServer(level, PortalCubedDamageSources.disintegration(level, entity), Float.MAX_VALUE)
			)
	);
	private static final DisintegrateEffect BREAK_ARMOR_STAND = register(
			"break_armor_stand",
			new DisintegrateEffect(
					entity -> entity instanceof ArmorStand,
					(level, entity) -> ((ArmorStandAccessor) entity).callBrokenByAnything(level, PortalCubedDamageSources.disintegration(level, entity))
			)
	);
	private static final DisintegrateEffect DESTROY_ITEM = register(
			"destroy_item",
			new DisintegrateEffect(
					entity -> entity instanceof ItemEntity,
					(_, entity) -> ((ItemEntity) entity).getItem().onDestroyed((ItemEntity) entity)
			)
	);
	private static final DisintegrateEffect END_THE_DRAGON_FIGHT = register(
			"end_the_dragon_fight",
			new DisintegrateEffect(
					entity -> entity instanceof EnderDragon,
					(level, entity) -> {
						if (level instanceof ServerLevel serverLevel) {
							EnderDragonFight fight = serverLevel.getDragonFight();
							if (fight != null) {
								fight.setDragonKilled((EnderDragon) entity);
							}
						}
					}
			)
	);
	private static final DisintegrateEffect REMOVE = register(
			"remove",
			new DisintegrateEffect(
					entity -> !(entity instanceof Player),
					(_, entity) -> entity.remove(Entity.RemovalReason.KILLED)
			)
	);

	private static DisintegrateEffect register(String name, DisintegrateEffect disintegrateEffect) {
		Identifier id = PortalCubed.id(name);
		return Registry.register(PortalCubedRegistries.DISINTEGRATE_EFFECT, id, disintegrateEffect);
	}

	public static void init() {
	}
}
