package io.github.fusionflux.portalcubed.content.prop.entity;

import java.util.Optional;
import java.util.OptionalInt;

import org.quiltmc.qsl.base.api.util.TriState;

import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import io.github.fusionflux.portalcubed.content.prop.HammerItem;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import io.github.fusionflux.portalcubed.framework.extension.CollisionListener;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import net.minecraft.core.Direction;
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

public class Prop extends Entity implements CollisionListener {
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Prop.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<OptionalInt> HELD_BY = SynchedEntityData.defineId(Prop.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
	//this value was obtained by converting the terminal velocity of props in source engine units to mc blocks
	private static final double TERMINAL_VELOCITY = 66.6667f;

	private int variantFromItem;
	private int lerpSteps;
	private double lerpX;
	private double lerpY;
	private double lerpZ;
	private double lerpYRot;

	public final PropType type;

	public Prop(PropType type, EntityType<?> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
		this.type = type;
	}

	protected Optional<Boolean> isDirty() {
		int variant = getVariant();
		return variant > 1 ? Optional.empty() : Optional.of(variant != 0);
	}

	protected void setDirty(boolean dirty) {
		setVariant(dirty ? 1 : 0);
	}

	public int getVariant() {
		return entityData.get(VARIANT);
	}

	public void setVariant(int variant) {
		if (!level().isClientSide) entityData.set(VARIANT, variant);
	}

	public void setVariantFromItem(int variant) {
		this.variantFromItem = variant;
	}

	public Optional<Player> getHeldBy() {
		var heldBy = entityData.get(HELD_BY);
		if (heldBy.isPresent())
			return Optional.of((Player) level().getEntity(heldBy.getAsInt()));
		return Optional.empty();
	}

	public boolean hold(Player player) {
		var heldBy = getHeldBy();
		boolean notHeld = heldBy.isEmpty() || level().getGameRules().getBoolean(PortalCubedGameRules.PROP_SNATCHING);
		if (!level().isClientSide && notHeld) {
			heldBy.ifPresent(holder -> ((PlayerExt) holder).pc$heldProp(OptionalInt.empty()));
			entityData.set(HELD_BY, OptionalInt.of(player.getId()));
		}
		return notHeld;
	}

	public void drop(Player player) {
		if (!level().isClientSide && getHeldBy().map(heldBy -> heldBy == player).orElse(false))
			entityData.set(HELD_BY, OptionalInt.empty());
	}

	@Override
	public void tick() {
		super.tick();
		var level = level();
		if (!level.isClientSide && (getType().is(PortalCubedEntityTags.CAN_BE_WASHED) && isDirty().orElse(false)) && isInWaterOrRain())
			setDirty(false);

		var heldBy = getHeldBy();
		if (!level.isClientSide || heldBy.map(holder -> holder.isLocalPlayer()).orElse(false)) {
			lerpSteps = 0;
			syncPacketPositionCodec(getX(), getY(), getZ());

			var vel = getDeltaMovement();
			if (heldBy.isEmpty()) {
				if (!isNoGravity())
					vel = vel.subtract(0, LivingEntity.DEFAULT_BASE_GRAVITY / (isInWater() ? 16 : 1), 0);
				var posBelow = getBlockPosBelowThatAffectsMyMovement();
				float f = level().getBlockState(posBelow).getBlock().getFriction();
				f = onGround() ? f * .91f : .91f;
				vel = new Vec3(vel.x * f, Math.max(vel.y, -TERMINAL_VELOCITY), vel.z * f);
				setDeltaMovement(vel);
				move(MoverType.SELF, vel);
			} else {
				var holder = heldBy.get();
				var holdPoint = holder.getEyePosition().add(Vec3.directionFromRotation(holder.getXRot(), holder.getYRot()).scale(2));
				float holdYOffset = -getBbHeight() / 2;
				setDeltaMovement(Vec3.ZERO);
				move(MoverType.PLAYER, position().vectorTo(holdPoint.add(0, holdYOffset, 0)));
				if (type != PropType.THE_TACO)
					setYRot((holder.getYRot() + 180) % 360);
				if (position().distanceToSqr(holder.getEyePosition()) >= 4.5 * 4.5) {
					drop(holder);
					((PlayerExt) holder).pc$heldProp(OptionalInt.empty());
				}
			}
		} else if (lerpSteps > 0) {
			double delta = 1.0 / lerpSteps;
			setPos(
				Mth.lerp(delta, getX(), lerpX),
				Mth.lerp(delta, getY(), lerpY),
				Mth.lerp(delta, getZ(), lerpZ)
			);
			setYRot((float) Mth.rotLerp(delta, getYRot(), lerpYRot));

			--lerpSteps;
		}
	}

	@Override
	public void lerpTo(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
		lerpX = x;
		lerpY = y;
		lerpZ = z;
		lerpYRot = yaw;
		lerpSteps = interpolationSteps;
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
		return getHeldBy().map(holder -> other != holder).orElse(true) && Boat.canVehicleCollide(this, other);
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
		if (!level.isClientSide && !isSilent()) {
			level.playSound(null, getX(), getY(), getZ(), type.soundType.impactSound, SoundSource.PLAYERS, 1, 1);
			level.gameEvent(this, GameEvent.HIT_GROUND, position());
		}
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(VARIANT, 0);
		entityData.define(HELD_BY, OptionalInt.empty());
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
