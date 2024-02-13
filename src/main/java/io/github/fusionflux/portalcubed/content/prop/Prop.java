package io.github.fusionflux.portalcubed.content.prop;

import java.util.OptionalInt;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.Vec3;

public class Prop extends Entity implements CollisionListener {
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Prop.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<OptionalInt> HELD_BY = SynchedEntityData.defineId(Prop.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
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

	protected boolean isDirty() {
		return getVariant() == 1;
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

	public OptionalInt getHeldBy() {
		return entityData.get(HELD_BY);
	}

	public boolean hold(Player player) {
		boolean notHeld = getHeldBy().isEmpty();
		if (!level().isClientSide && notHeld)
			entityData.set(HELD_BY, OptionalInt.of(player.getId()));
		return notHeld;
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
		if (!level().isClientSide && isInWaterOrRain())
			setDirty(false);
		if (isControlledByLocalInstance()) {
			lerpSteps = 0;
			syncPacketPositionCodec(getX(), getY(), getZ());

			var vel = getDeltaMovement();
			if (getHeldBy().isEmpty()) {
				if (!isNoGravity())
					vel = vel.subtract(0, LivingEntity.DEFAULT_BASE_GRAVITY / (isInWater() ? 16 : 1), 0);
				var posBelow = getBlockPosBelowThatAffectsMyMovement();
				float f = level().getBlockState(posBelow).getBlock().getFriction();
				f = onGround() ? f * .91f : .91f;
				vel = new Vec3(vel.x * f, Math.max(vel.y, -TERMINAL_VELOCITY), vel.z * f);
				setDeltaMovement(vel);
				move(MoverType.SELF, vel);
			} else {
				var player = ((Player) level().getEntity(getHeldBy().getAsInt()));
				var holdPoint = player.getEyePosition().add(Vec3.directionFromRotation(player.getXRot(), player.getYRot()).scale(2));
				float holdYOffset = -getBbHeight() / 2;
				move(MoverType.PLAYER, position().vectorTo(holdPoint.add(0, holdYOffset, 0)));
				if (type != PropType.THE_TACO)
					setYRot(-player.getYRot() % 360);
				if (position().distanceToSqr(player.getEyePosition()) >= 4.5 * 4.5) {
					drop(player);
					((PlayerExt) player).pc$heldProp(OptionalInt.empty());
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
		if (type.hasDirtyVariant) {
			if (player.getAbilities().mayBuild && itemInHand.is(PortalCubedItemTags.AGED_CRAFTING_MATERIALS) && !isDirty()) {
				if (level instanceof ServerLevel serverLevel) {
					setDirty(true);
					serverLevel.playSound(null, this, SoundType.VINE.getPlaceSound(), SoundSource.PLAYERS, 1, .5f);
					var particleOption = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.VINE.defaultBlockState());
					for (var dir : Direction.values()) {
						double x = getX() + (dir.getStepX() * getBbWidth() / 2);
						double y = getY() + (dir.getStepY() * getBbWidth() / 2);
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
	public boolean hurt(DamageSource source, float amount) {
		var level = level();
		if (!level.isClientSide) {
			if (source.isCreativePlayer()) {
				remove(RemovalReason.KILLED);
			} else if (source.getEntity() instanceof Player player && player.getMainHandItem().is(PortalCubedItems.HAMMER)) {
				HammerItem.destroyProp(player, level(), this);
			}
		}
		return true;
	}

	@Override
	public boolean skipAttackInteraction(Entity attacker) {
		if (attacker instanceof Player player)
			return !(player.getAbilities().instabuild || player.getMainHandItem().is(PortalCubedItems.HAMMER));
		return true;
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
	public void onCollision() {
		var level = level();
		if (!level.isClientSide)
			level.playSound(null, getX(), getY(), getZ(), type.soundType.impactSound, SoundSource.PLAYERS, 1, 1);
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
