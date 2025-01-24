package io.github.fusionflux.portalcubed.content.prop.entity;

import java.util.Optional;
import java.util.function.Predicate;

import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;
import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.prop.HammerItem;
import io.github.fusionflux.portalcubed.content.prop.ImpactSoundType;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class Prop extends HoldableEntity {
	// Max speed of a dropped prop, to avoid flinging things cross chambers
	public static final double MAX_SPEED_SQR = Mth.square(0.9);
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Prop.class, EntityDataSerializers.INT);
	// Terminal velocity of props in source units converted to blocks/tick
	private static final double TERMINAL_VELOCITY = 66.6667f / 20f;
	// Arbitrary limit to prevent use against high-health mobs, for example; wardens
	private static final float MAX_FALL_DAMAGE = 2 * 30;
	// Makes it so that it takes roughly the same amount of fall distance as portal 1 to kill a player
	private static final float FALL_DAMAGE_PER_BLOCK = 2 * 1.5f;
	// Makes it so the damage applies even when the collision box is outside the target
	private static final double CHECK_BOX_EPSILON = 1E-7;
	public static final String VARIANT_KEY = "variant";
	public static final String VARIANT_FROM_ITEM_KEY = "variant_from_item";
	public final PropType type;
	private final SoundEvent impactSound;

	private int variantFromItem;
	private boolean sideColliding;
	private boolean topColliding;
	private boolean bottomColliding;

	public Prop(PropType type, EntityType<?> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
		this.type = type;
		this.impactSound = ImpactSoundType.forEntityType(entityType)
				.map(ImpactSoundType::sound)
				.orElse(PortalCubedSounds.FIDDLE_STICKS);
	}

	public Optional<Boolean> isDirty() {
		int variant = this.getVariant();
		return variant > 1 ? Optional.empty() : Optional.of(variant != 0);
	}

	public void setDirty(boolean dirty) {
		this.setVariant(dirty ? 1 : 0);
	}

	public int getVariant() {
		return this.entityData.get(VARIANT);
	}

	public void setVariant(int variant) {
		if (variant < 0) variant = 0;
		if (!this.level().isClientSide)
			this.entityData.set(VARIANT, variant);
	}

	public void setVariantFromItem(int variant) {
		if (variant < 0) variant = 0;
		this.variantFromItem = variant;
	}

	@Override
	protected double getDefaultGravity() {
		return LivingEntity.DEFAULT_BASE_GRAVITY;
	}

	@Override
	public void tick() {
		super.tick();
		Level level = this.level();
		if (level.isClientSide)
			return;

		this.tickState();

		// apply gravity and friction when not held
		if (!this.isHeld()) {
			this.applyGravity();
			Vec3 vel = this.getDeltaMovement();

			// friction logic from LivingEntity
			BlockPos posBelow = this.getBlockPosBelowThatAffectsMyMovement();
			float friction = this.level().getBlockState(posBelow).getBlock().getFriction();
			friction = this.onGround() ? friction * .91f : .91f;
			vel = new Vec3(vel.x * friction, vel.y, vel.z * friction);

			// speed caps
			if (vel.length() > MAX_SPEED_SQR) {
				double y = vel.y;
				vel = vel.normalize().scale(MAX_SPEED_SQR);
				// downwards speed is special
				if (y < 0) {
					double newY = Math.max(y, -TERMINAL_VELOCITY);
					vel = vel.with(Axis.Y, newY);
				}
			}

			this.setDeltaMovement(vel);
			this.move(MoverType.SELF, vel);
			this.applyEffectsFromBlocks();
		}
	}

	protected void tickState() {
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
		ItemStack itemInHand = player.getItemInHand(hand);
		Level world = this.level();
		if (getType().is(PortalCubedEntityTags.CAN_BE_DIRTY)) {
			if (player.getAbilities().mayBuild && itemInHand.is(PortalCubedItemTags.AGED_CRAFTING_MATERIALS) && isDirty().map(v -> !v).orElse(false)) {
				if (world instanceof ServerLevel serverLevel) {
					setDirty(true);
					serverLevel.playSound(null, this, SoundType.VINE.getPlaceSound(), SoundSource.PLAYERS, 1, .5f);
					BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.VINE.defaultBlockState());
					for (Direction dir : Direction.values()) {
						double x = getX() + (dir.getStepX() * getBbWidth() / 2);
						double y = getY() + (dir.getStepY() * getBbHeight() / 2);
						double z = getZ() + (dir.getStepZ() * getBbWidth() / 2);
						serverLevel.sendParticles(particleOption, x, y, z, random.nextInt(5, 8), 0, 0, 0, 1);
					}
					itemInHand.shrink(1);
				}
				return InteractionResult.SUCCESS;
			}
		}
		return super.interact(player, hand);
	}

	@Override
	public void setRemainingFireTicks(int ticks) {
		super.setRemainingFireTicks(ticks);
		if (this.getType().is(PortalCubedEntityTags.CAN_BE_CHARRED) && this.getRemainingFireTicks() > 0)
			this.setDirty(true);
	}

	public boolean isInvulnerableTo(DamageSource source) {
		if (this.isInvulnerable() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
			return true;

		if (source.getDirectEntity() instanceof Player player) {
			return !player.getAbilities().instabuild && !HammerItem.usingHammer(player);
		}

		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (this.isInvulnerableTo(source))
			return false;

		if (shouldDropLoot(source))
			this.dropLoot(level, source);

		this.kill(level);
		return true;
	}

	protected void dropLoot(ServerLevel level, DamageSource source) {
		Optional<ResourceKey<LootTable>> maybeLootTable = this.getLootTable();
		if (maybeLootTable.isEmpty())
			return;

		LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(maybeLootTable.get());
		LootParams.Builder builder = new LootParams.Builder(level)
				.withParameter(LootContextParams.THIS_ENTITY, this)
				.withParameter(LootContextParams.ORIGIN, this.position())
				.withParameter(LootContextParams.DAMAGE_SOURCE, source);
		LootParams params = builder.create(LootContextParamSets.ENTITY);
		lootTable.getRandomItems(params, 0, stack -> this.spawnAtLocation(level, stack));
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
		return !this.isRemoved();
	}

	@Override
	public ItemStack getPickResult() {
		ItemStack stack = this.type.item().getDefaultInstance();
		if (this.variantFromItem != 0)
			stack.set(PortalCubedDataComponents.PROP_VARIANT, this.variantFromItem);
		return stack;
	}

	@Override
	public void move(MoverType type, Vec3 movement) {
		super.move(type, movement);

		if (!this.level().isClientSide) {
			if (this.horizontalCollision) {
				if (!this.sideColliding)
					this.onCollision();
				this.sideColliding = true;
			} else {
				this.sideColliding = false;
			}

			if (this.verticalCollision) {
				if (!this.topColliding)
					this.onCollision();
				this.topColliding = true;
			} else {
				this.topColliding = false;
			}

			if (this.verticalCollisionBelow) {
				if (!this.bottomColliding)
					this.onCollision();
				this.bottomColliding = true;
			} else {
				this.bottomColliding = false;
			}
		}
	}

	protected void onCollision() {
		this.playSound(this.impactSound);
		this.gameEvent(GameEvent.HIT_GROUND);
	}

	@Override
	protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
		if (this.level() instanceof ServerLevel world && this.getType().is(PortalCubedEntityTags.DEALS_LANDING_DAMAGE)) {
			int blocksFallen = Mth.ceil(this.fallDistance);
			if (blocksFallen > 0) {
				float damage = Math.min(FALL_DAMAGE_PER_BLOCK * blocksFallen, MAX_FALL_DAMAGE);
				Predicate<Entity> selector = EntitySelector.NO_CREATIVE_OR_SPECTATOR
						.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE)
						.and(this::notHeldBy);
				world.getEntities(this, this.getBoundingBox().expandTowards(0, -CHECK_BOX_EPSILON, 0), selector).forEach(
						entity -> entity.hurtServer(world, PortalCubedDamageSources.landingDamage(world, this, entity), damage)
				);
			}
		}
		super.checkFallDamage(y, onGround, state, pos);
	}

	@Override
	protected void defineSynchedData(Builder builder) {
		super.defineSynchedData(builder);
		builder.define(VARIANT, 0);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putInt(VARIANT_KEY, this.getVariant());
		tag.putInt(VARIANT_FROM_ITEM_KEY, this.variantFromItem);

		CompoundTag collisionTag = new CompoundTag();
		collisionTag.putBoolean("side", this.sideColliding);
		collisionTag.putBoolean("top", this.topColliding);
		collisionTag.putBoolean("bottom", this.bottomColliding);
		tag.put("collision", collisionTag);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		this.setVariant(tag.getInt(VARIANT_KEY));
		this.setVariantFromItem(tag.getInt(VARIANT_FROM_ITEM_KEY));

		CompoundTag collisionTag = tag.getCompound("collision");
		this.sideColliding = collisionTag.getBoolean("side");
		this.topColliding = collisionTag.getBoolean("top");
		this.bottomColliding = collisionTag.getBoolean("bottom");
	}

	private static boolean shouldDropLoot(DamageSource source) {
		if (!(source.getDirectEntity() instanceof Player player))
			return false;

		if (player.getAbilities().instabuild)
			return false;

		return HammerItem.usingHammer(player);
	}
}
