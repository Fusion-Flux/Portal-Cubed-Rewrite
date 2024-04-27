package io.github.fusionflux.portalcubed.content.misc;

import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;
import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;

import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

public class Lemonade extends ThrowableItemProjectile {
	public static final int TICKS_PER_TICK = 20;
	public static final int DING_TICK = 10;
	private static final float MIN_EXPLOSION_POWER = 1.5f;
	private static final float MAX_EXPLOSION_POWER = 2.5f;

	public int explodeTicks;

	public Lemonade(EntityType<Lemonade> entityType, Level level) {
		super(entityType, level);
	}

	public Lemonade(Level world, LivingEntity owner) {
		super(PortalCubedEntities.LEMONADE, owner, world);
	}

	public Lemonade(Level world, double x, double y, double z) {
		super(PortalCubedEntities.LEMONADE, x, y, z, world);
	}

	public static Lemonade create(EntityType<Lemonade> entityType, Level level) {
		return new Lemonade(entityType, level);
	}

	@Override
	public void tick() {
		if (level() instanceof ServerLevel level) {
			if (--explodeTicks <= 0) {
				level.explode(
						this,
						PortalCubedDamageSources.lemonade(level, this, getOwner() instanceof LivingEntity livingEntity ? livingEntity : null),
						null,
						position(),
						Math.max(this.random.nextFloat() * MAX_EXPLOSION_POWER, MIN_EXPLOSION_POWER),
						true,
						Level.ExplosionInteraction.NONE
				);
				discard();
				return;
			}
			if (explodeTicks == DING_TICK) playSound(PortalCubedSounds.timerDing(random));
			if (explodeTicks % TICKS_PER_TICK == 0 && explodeTicks != 0) playSound(PortalCubedSounds.OLD_AP_TIMER);
		}
		super.tick();
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.explodeTicks = tag.getInt("explode_ticks");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("explode_ticks", this.explodeTicks);
	}

	@NotNull
	@Override
	protected Item getDefaultItem() {
		return PortalCubedItems.LEMONADE;
	}
}
