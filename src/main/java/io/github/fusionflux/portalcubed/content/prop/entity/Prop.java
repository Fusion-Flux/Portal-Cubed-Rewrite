package io.github.fusionflux.portalcubed.content.prop.entity;

import java.util.Optional;

import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;
import io.github.fusionflux.portalcubed.content.prop.HammerItem;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.framework.extension.CollisionListener;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class Prop extends HoldableEntity implements CollisionListener {
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Prop.class, EntityDataSerializers.INT);
	//this value was obtained by converting the terminal velocity of props in source engine units to mc blocks
	private static final double TERMINAL_VELOCITY = 66.6667f;
	//arbitrary limit to prevent use against high-health mobs, for example; wardens
	private static final float MAX_FALL_DAMAGE = 2 * 30;
	//makes it so that it takes roughly the same amount of fall distance as portal 1 to kill a player
	private static final float FALL_DAMAGE_PER_BLOCK = 2 * 1.5f;
	//makes it so the damage applies even when the collision box is outside the target
	private static final double CHECK_BOX_EPSILON = 1E-7;

	public final PropType type;
	private int variantFromItem;

	public Prop(PropType type, EntityType<?> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
		this.type = type;
	}

	protected Optional<Boolean> isDirty() {
		int variant = this.getVariant();
		return variant > 1 ? Optional.empty() : Optional.of(variant != 0);
	}

	protected void setDirty(boolean dirty) {
		this.setVariant(dirty ? 1 : 0);
	}

	public int getVariant() {
		return this.entityData.get(VARIANT);
	}

	public void setVariant(int variant) {
		if (!this.level().isClientSide)
			this.entityData.set(VARIANT, variant);
	}

	public void setVariantFromItem(int variant) {
		this.variantFromItem = variant;
	}

	@Override
	public void tick() {
		super.tick();
		this.tickState();

		// apply gravity and friction when not held
		if (!this.isHeld()) {
			Vec3 vel = this.getDeltaMovement();
			if (!this.isNoGravity()) { // gravity
				double gravity = LivingEntity.DEFAULT_BASE_GRAVITY / (isInWater() ? 16 : 1);
				vel = vel.subtract(0, gravity, 0);
			}
			// friction logic from LivingEntity
			BlockPos posBelow = this.getBlockPosBelowThatAffectsMyMovement();
			float friction = this.level().getBlockState(posBelow).getBlock().getFriction();
			friction = this.onGround() ? friction * .91f : .91f;
			vel = new Vec3(vel.x * friction, vel.y, vel.z * friction);
			// terminal velocity
			if (vel.y < -TERMINAL_VELOCITY) {
				vel = vel.with(Axis.Y, -TERMINAL_VELOCITY);
			}

			this.setDeltaMovement(vel);
			this.move(MoverType.SELF, this.getDeltaMovement());
		}
	}

	protected void tickState() {
		Level level = this.level();
		if (level.isClientSide)
			return;

		boolean dirty = this.isDirty().orElse(false);
		if (!dirty)
			return;

		if (this.getType().is(PortalCubedEntityTags.CAN_BE_WASHED) && this.isInWaterOrRain())
			this.setDirty(false);
	}

	@Override
	protected boolean facesHolder() {
		return this.type.facesPlayer;
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		var itemInHand = player.getItemInHand(hand);
		var level = level();
		if (getType().is(PortalCubedEntityTags.CAN_BE_DIRTY)) {
			if (player.getAbilities().mayBuild && itemInHand.is(PortalCubedItemTags.AGED_CRAFTING_MATERIALS) && isDirty().map(v -> !v).orElse(false)) {
				if (level instanceof ServerLevel serverLevel) {
					setDirty(true);
					serverLevel.playSound(null, this, SoundType.VINE.getPlaceSound(), SoundSource.PLAYERS, 1, .5f);
					var particleOption = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.VINE.defaultBlockState());
					for (var dir : Direction.values()) {
						double x = getX() + (dir.getStepX() * getBbWidth() / 2);
						double y = getY() + (dir.getStepY() * getBbHeight() / 2);
						double z = getZ() + (dir.getStepZ() * getBbWidth() / 2);
						serverLevel.sendParticles(particleOption, x, y, z, random.nextInt(5, 8), 0, 0, 0, 1);
					}
					itemInHand.shrink(1);
				}
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}
		return super.interact(player, hand);
	}

	@Override
	public void setRemainingFireTicks(int ticks) {
		super.setRemainingFireTicks(ticks);
		if (getType().is(PortalCubedEntityTags.CAN_BE_CHARRED) && getRemainingFireTicks() > 0) setDirty(true);
	}

	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		if (source.getDirectEntity() instanceof Player player) {
			var abilities = player.getAbilities();
			return (isInvulnerable() && !abilities.instabuild) || !(abilities.instabuild || (abilities.mayBuild && HammerItem.usingHammer(player)));
		}
		return isRemoved() || !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (!isInvulnerableTo(source)) {
			if (!level().isClientSide) {
				if (!(source.getDirectEntity() instanceof Player player && (player.getAbilities().instabuild && !HammerItem.usingHammer(player))))
					dropLoot(source);
				kill();
			}
			return true;
		}
		return false;
	}

	protected void dropLoot(DamageSource source) {
		if (level() instanceof ServerLevel level) {
			var lootTableId = getType().getDefaultLootTable();
			var lootTable = level.getServer().getLootData().getLootTable(lootTableId);
			var builder = new LootParams.Builder(level)
				.withParameter(LootContextParams.THIS_ENTITY, this)
				.withParameter(LootContextParams.ORIGIN, position())
				.withParameter(LootContextParams.DAMAGE_SOURCE, source);
			lootTable.getRandomItems(builder.create(LootContextParamSets.ENTITY), 0, this::spawnAtLocation);
		}
	}

	@Override
	public boolean canCollideWith(Entity other) {
		return other != this.getHolder() && Boat.canVehicleCollide(this, other);
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	public boolean isPickable() {
		return !isRemoved();
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(type.item());
	}

	@Override
	public void onCollision() {
		var level = level();
		if (!level.isClientSide) {
			if (!isSilent()) {
				level.playSound(null, getX(), getY(), getZ(), type.soundType.impactSound, SoundSource.PLAYERS, 1, 1);
				level.gameEvent(this, GameEvent.HIT_GROUND, position());
			}

			if (getType().is(PortalCubedEntityTags.DEALS_LANDING_DAMAGE) && verticalCollisionBelow) {
				int blocksFallen = Mth.ceil(fallDistance);
				if (blocksFallen > 0) {
					float damage = Math.min(FALL_DAMAGE_PER_BLOCK * blocksFallen, MAX_FALL_DAMAGE);
					var selector =
						EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(
						EntitySelector.LIVING_ENTITY_STILL_ALIVE).and(
						entity -> !(entity instanceof PlayerExt ext && ext.pc$getHeldProp().orElse(-1) == getId()));
					level.getEntities(this, getBoundingBox().inflate(CHECK_BOX_EPSILON), selector)
						.forEach(entity -> entity.hurt(PortalCubedDamageSources.landingDamage(level, this, entity), damage));
				}
			}
		}
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(VARIANT, 0);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putInt("variant", getVariant());
		tag.putInt("variant_from_item", variantFromItem);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		setVariant(tag.getInt("variant"));
		setVariantFromItem(tag.getInt("variant_from_item"));
	}
}
