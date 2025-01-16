package io.github.fusionflux.portalcubed.content.lemon;

import io.github.fusionflux.portalcubed.framework.entity.EntityReference;
import io.github.fusionflux.portalcubed.framework.entity.LerpableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.Entity;

import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;

import net.minecraft.world.phys.Vec3;

import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;
import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

public class Lemonade extends LerpableEntity implements ItemSupplier, TraceableEntity {
	public static final EntityDataAccessor<ItemStack> STACK = SynchedEntityData.defineId(Lemonade.class, EntityDataSerializers.ITEM_STACK);
	public static final int TICKS_PER_TIMER_TICK = 20;
	public static final int DING_TICK = 10;
	public static final int MAX_ARM_TIME = (20 * 5) + DING_TICK;
	private static final float MIN_EXPLOSION_POWER = 1.5f;
	private static final float MAX_EXPLOSION_POWER = 2.5f;

	private EntityReference thrower = EntityReference.EMPTY;
	private int explodeTicks = MAX_ARM_TIME;

	public Lemonade(Level level, ItemStack stack, @Nullable Entity thrower, int explodeTicks) {
		this(PortalCubedEntities.LEMONADE, level);

		if (!stack.isEmpty()) {
			this.setItem(stack);
		}

		this.thrower = EntityReference.of(thrower);
		this.explodeTicks = explodeTicks;
	}

	public Lemonade(EntityType<Lemonade> entityType, Level level) {
		super(entityType, level);
	}

	public static Lemonade create(EntityType<Lemonade> entityType, Level level) {
		return new Lemonade(entityType, level);
	}

	public void doThrow(Vec3 pos, Vec3 normalizedDirection, float power) {
		if (this.level() instanceof ServerLevel serverLevel) {
			this.setPos(pos);
			Entity thrower = this.thrower.get(serverLevel);
			Vec3 sourceVel = getEffectiveSourceVelocity(thrower);
			Vec3 vel = normalizedDirection.scale(power).add(sourceVel);
			this.setDeltaMovement(vel);
			this.hasImpulse = true;
		}
	}

	@Override
	protected void defineSynchedData(Builder builder) {
		builder.define(STACK, new ItemStack(PortalCubedItems.LEMONADE));
	}

	public void setItem(ItemStack stack) {
		this.entityData.set(STACK, stack.copyWithCount(1));
	}

	@Override
	public ItemStack getItem() {
		return this.entityData.get(STACK);
	}

	@Override
	@Nullable
	public Entity getOwner() {
		return this.thrower.tryGet(this.level());
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level() instanceof ServerLevel serverLevel) {
			this.tickMotion();
			this.tickExplosion(serverLevel);
		}
	}

	private void tickMotion() {
		this.applyGravity();
		Vec3 vel = this.getDeltaMovement();

		// friction logic from LivingEntity
		BlockPos posBelow = this.getBlockPosBelowThatAffectsMyMovement();
		float friction = this.level().getBlockState(posBelow).getBlock().getFriction();
		friction = this.onGround() ? friction * .91f : .91f;
		vel = new Vec3(vel.x * friction, vel.y, vel.z * friction);

		this.setDeltaMovement(vel);
		this.move(MoverType.SELF, vel);
		this.applyEffectsFromBlocks();
		this.handlePortal();
	}

	private void tickExplosion(ServerLevel level) {
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

		if (this.explodeTicks == DING_TICK) {
			this.playSound(PortalCubedSounds.timerDing(this.random));
		}

		if (this.explodeTicks % TICKS_PER_TIMER_TICK == 0 && this.explodeTicks != 0)
			this.playSound(PortalCubedSounds.OLD_AP_TIMER);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}

	@Override
	protected double getDefaultGravity() {
		return 0.05; // same as AbstractArrow
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		ItemStack.parse(this.registryAccess(), tag.getCompound("stack")).ifPresent(this::setItem);
		this.thrower = EntityReference.parse(tag, "thrower");
		this.explodeTicks = tag.getInt("explode_ticks");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		tag.put("stack", this.getItem().save(this.registryAccess()));
		this.thrower.save(tag, "thrower");
		tag.putInt("explode_ticks", this.explodeTicks);
	}

	private static Vec3 getEffectiveSourceVelocity(@Nullable Entity thrower) {
		if (thrower != null) {
			Vec3 vel = thrower.getKnownMovement();
			return thrower.onGround() ? vel.with(Axis.Y, 0) : vel;
		}

		return Vec3.ZERO;
	}
}
