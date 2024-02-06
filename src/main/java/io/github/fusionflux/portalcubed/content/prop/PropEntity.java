package io.github.fusionflux.portalcubed.content.prop;

import java.util.OptionalInt;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PropEntity extends Entity {
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(PropEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<OptionalInt> HELD_BY = SynchedEntityData.defineId(PropEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
	private static final double TERMINAL_VELOCITY = 66.6667f;

	private int lerpSteps;
	private double lerpX;
	private double lerpY;
	private double lerpZ;
	private double lerpYRot;

	public final PropType type;

	public PropEntity(PropType type, EntityType<?> entityType, Level level) {
		super(entityType, level);
		this.type = type;
	}

	public int getVariant() {
		return entityData.get(VARIANT);
	}

	public void setVariant(int variant) {
		if (!level().isClientSide) entityData.set(VARIANT, variant);
	}

	public OptionalInt getHeldBy() {
		return entityData.get(HELD_BY);
	}

	public boolean hold(Player player) {
		boolean notHeld = getHeldBy().isEmpty();
		if (!level().isClientSide && notHeld)
			entityData.set(HELD_BY, OptionalInt.of(player.getId()));
		return !notHeld;
	}

	public void drop(Player player) {
		if (!level().isClientSide && getHeldBy().orElse(-1) == player.getId())
			entityData.set(HELD_BY, OptionalInt.empty());
	}

	@Override
	public boolean isControlledByLocalInstance() {
		if (getHeldBy().isPresent()) {
			var playerHolding = ((Player) level().getEntity(getHeldBy().getAsInt()));
			return isEffectiveAi() || playerHolding.isLocalPlayer();
		}
		return isEffectiveAi();
	}

	@Override
	public void tick() {
		super.tick();
		if (isControlledByLocalInstance()) {
			lerpSteps = 0;
			syncPacketPositionCodec(getX(), getY(), getZ());

			var vel = getDeltaMovement();
			if (getHeldBy().isEmpty()) {
				if (!onGround() && !isNoGravity()) {
					vel = vel.subtract(0, LivingEntity.DEFAULT_BASE_GRAVITY / (isInWater() ? 16 : 1), 0);
				}
				var posBelow = this.getBlockPosBelowThatAffectsMyMovement();
				float f = level().getBlockState(posBelow).getBlock().getFriction();
				f = onGround() ? f * .91f : .91f;
				vel = new Vec3(vel.x * f, Math.max(vel.y, -TERMINAL_VELOCITY), vel.z * f);
			} else {
				var player = ((Player) level().getEntity(getHeldBy().getAsInt()));
				var holdPoint = player.getEyePosition().add(Vec3.directionFromRotation(player.getXRot(), player.getYRot()).scale(2));
				float holdYOffset = -getBbHeight() / 2;
				vel = position().vectorTo(holdPoint.add(0, holdYOffset, 0));
				setYRot(-player.getYRot() % 360);
			}
			setDeltaMovement(vel);
			move(MoverType.SELF, vel);
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
		lerpSteps = getType().updateInterval();
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
		return new ItemStack(PropType.ITEMS.get(type));
	}

	@Override
	public void lavaHurt() {
		super.lavaHurt();
		if (type == PropType.PORTAL_1_COMPANION_CUBE && getVariant() == 0) setVariant(1);
	}

	@Override
	public void setRemainingFireTicks(int ticks) {
		super.setRemainingFireTicks(ticks);
		if (type == PropType.PORTAL_1_COMPANION_CUBE && getVariant() == 0 && getRemainingFireTicks() > 0) setVariant(1);
	}

	@Override
	public void extinguishFire() {
		super.extinguishFire();
		if (type == PropType.PORTAL_1_COMPANION_CUBE && getVariant() == 1) setVariant(0);
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(VARIANT, 0);
		entityData.define(HELD_BY, OptionalInt.empty());
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putInt("CustomModelData", getVariant());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		setVariant(tag.getInt("CustomModelData"));
	}
}
