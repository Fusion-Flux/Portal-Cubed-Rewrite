package io.github.fusionflux.portalcubed.content.lemon;

import org.jetbrains.annotations.NotNull;

import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;
import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Lemonade extends ThrowableItemProjectile {
	public static final int TICKS_PER_TIMER_TICK = 20;
	public static final int DING_TICK = 10;
	public static final int MAX_ARM_TIME = (20 * 5) + DING_TICK;
	private static final float MIN_EXPLOSION_POWER = 1.5f;
	private static final float MAX_EXPLOSION_POWER = 2.5f;

	public int explodeTicks;

	public Lemonade(EntityType<Lemonade> entityType, Level level) {
		super(entityType, level);
	}

	public Lemonade(LivingEntity owner, Level level, ItemStack stack) {
		super(PortalCubedEntities.LEMONADE, owner, level, stack);
	}

	public Lemonade(Level world, double x, double y, double z, ItemStack stack) {
		super(PortalCubedEntities.LEMONADE, x, y, z, world, stack);
	}

	public static Lemonade create(EntityType<Lemonade> entityType, Level level) {
		return new Lemonade(entityType, level);
	}

	@Override
	public void tick() {
		if (this.level() instanceof ServerLevel level) {
			this.explodeTicks--;
			if (this.explodeTicks <= 0) {
				DamageSource source = PortalCubedDamageSources.lemonade(
						level, this, this.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null
				);

				level.explode(
						this, source, null, this.position(),
						Math.max(this.random.nextFloat() * MAX_EXPLOSION_POWER, MIN_EXPLOSION_POWER),
						true, Level.ExplosionInteraction.NONE
				);

				this.discard();
				return;
			}
			if (this.explodeTicks == DING_TICK)
				this.playSound(PortalCubedSounds.timerDing(this.random));

			if (this.explodeTicks % TICKS_PER_TIMER_TICK == 0 && this.explodeTicks != 0)
				this.playSound(PortalCubedSounds.OLD_AP_TIMER);
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
