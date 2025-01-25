package io.github.fusionflux.portalcubed.mixin.disintegration;

import java.util.ArrayList;
import java.util.Collection;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.PortalCubedParticles;
import io.github.fusionflux.portalcubed.content.PortalCubedStats;
import io.github.fusionflux.portalcubed.content.button.FloorButtonBlock;
import io.github.fusionflux.portalcubed.content.fizzler.DisintegrateEffect;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.extension.DisintegrationExt;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.DisintegratePacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public abstract class EntityMixin implements DisintegrationExt {
	@Shadow
	public abstract Level level();

	@Shadow
	public abstract void gameEvent(Holder<GameEvent> gameEvent);

	@Shadow
	public abstract BlockState getBlockStateOn();

	@Shadow
	public abstract BlockPos blockPosition();

	@Shadow
	public abstract void setDeltaMovement(Vec3 deltaMovement);

	@Shadow
	public abstract void stopRiding();

	@Shadow
	public abstract Vec3 getDeltaMovement();

	@Shadow
	public abstract void move(MoverType type, Vec3 movement);

	@Shadow
	public abstract EntityType<?> getType();

	@Shadow
	public abstract AABB getBoundingBox();

	@Shadow
	@Final
	protected RandomSource random;

	@Shadow
	public abstract double getX();

	@Shadow
	public abstract double getY();

	@Shadow
	public abstract double getZ();

	@Unique
	private boolean disintegrating;
	@Unique
	private int disintegrateTicks;

	@Override
	public boolean pc$disintegrate() {
		if (!this.disintegrating) {
			if (this.level() instanceof ServerLevel) {
				this.gameEvent(GameEvent.ENTITY_DIE);

				Entity self = (Entity) (Object) this;

				// In portal buttons push back on the objects that are on them, disintegration makes objects lose all their mass, so they get ejected but we cant do that here so lets just apply a slight force
				BlockState feetState = this.getBlockStateOn();
				if (feetState.getBlock() instanceof FloorButtonBlock floorButton && floorButton.isEntityPressing(feetState, this.blockPosition(), self))
					this.setDeltaMovement(feetState.getValue(FloorButtonBlock.FACE).getUnitVec3().scale(FloorButtonBlock.DISINTEGRATION_EJECTION_FORCE));

				this.disintegrateTicks = DISINTEGRATE_TICKS;

				Collection<ServerPlayer> tracking = PlayerLookup.tracking(self);
				if ((Object) this instanceof ServerPlayer player) {
					tracking = new ArrayList<>(tracking);
					tracking.add(player);
					player.awardStat(PortalCubedStats.TIMES_DISINTEGRATED);
				}

				if (!tracking.isEmpty()) {
					DisintegratePacket packet = new DisintegratePacket(self);
					for (ServerPlayer toUpdate : tracking) {
						PortalCubedPackets.sendToClient(toUpdate, packet);
					}
				}

				this.stopRiding();
			}
			this.disintegrating = true;
			return true;
		}
		return false;
	}

	@Override
	public void pc$disintegrate(int ticks) {
		if (this.pc$disintegrate()) {
			this.disintegrateTicks = ticks;
		}
	}

	@Override
	public boolean pc$disintegrating() {
		return this.disintegrating;
	}

	@Override
	public void pc$disintegrating(boolean disintegrating) {
		this.disintegrating = disintegrating;
	}

	@Override
	public int pc$disintegrateTicks() {
		return this.disintegrateTicks;
	}

	@Override
	public void pc$disintegrateTick() {
		Vec3 velocity = this.getDeltaMovement().scale(.91);
		this.move(MoverType.SELF, velocity);
		this.setDeltaMovement(velocity);

		Level world = this.level();
		--this.disintegrateTicks;
		if (world instanceof ServerLevel serverWorld) {
			if (this.disintegrateTicks <= 0)
				DisintegrateEffect.applyAll(serverWorld, (Entity) (Object) this);
		} else {
			if (this.disintegrateTicks > TRANSLUCENCY_START_TICKS)
				this.spawnDisintegrationParticles(world);
		}
	}

	@Unique
	private void spawnDisintegrationParticles(Level world) {
		EntityType<?> type = this.getType();

		AABB bb = this.getBoundingBox();
		double width = bb.getXsize();
		double height = bb.getYsize();
		double length = bb.getZsize();

		if (!type.is(PortalCubedEntityTags.FIZZLES_WITHOUT_DARK_PARTICLES)) { // Portal 1 props don't make ash when fizzled
			double volume = width * height * length;
			for (int i = 0; i < Math.min(Math.max(Math.round(volume*15), 1), 100); i++) { // Capped to 100/tick so that fizzling something large doesn't instantly kill performance.  Use the largest of 15*volume/tick OR 1/tick, to prevent entities with small volumes (mug, item) from not making ash
				double xOffset = this.random.nextGaussian() * (width / 3);
				double yOffset = .2 + (this.random.nextGaussian() * (height / 3));
				double zOffset = this.random.nextGaussian() * (length / 3);
				double velocityX = this.random.nextGaussian();
				double velocityY = this.random.nextGaussian();
				double velocityZ = this.random.nextGaussian();
				world.addParticle(PortalCubedParticles.FIZZLE_DARK, this.getX() + xOffset, this.getY() + yOffset, this.getZ() + zOffset, velocityX, velocityY, velocityZ);
			}
		}

		// Some props don't make the bright particles, and Portal 1 props have an alternate bright particle type
		Vec3 center = this.getBoundingBox().getCenter();
		if (!type.is(PortalCubedEntityTags.FIZZLES_WITHOUT_BRIGHT_PARTICLES)) {
			if (!type.is(PortalCubedEntityTags.FIZZLES_WITH_ALTERNATE_BRIGHT_PARTICLES)) {
				for (int i = 0; i < 3; i++) {
					world.addParticle(PortalCubedParticles.FIZZLE_BRIGHT, center.x, center.y, center.z, 0, 0, 0);
				}
			} else {
				for (int i = 0; i < 3; i++) {
					double xOffset = this.random.nextGaussian() * (width / 3.8);
					double yOffset = .2 + (this.random.nextGaussian() * (height / 3.8));
					double zOffset = this.random.nextGaussian() * (length / 3.8);
					world.addParticle(PortalCubedParticles.FIZZLE_BRIGHT_ALTERNATE, this.getX() + xOffset, this.getY() + yOffset, this.getZ() + zOffset, 0, 0, 0);
				}
			}
		}
	}

	@WrapOperation(method = "rideTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
	private void disintegrationTick(Entity instance, Operation<Void> original) {
		if (!this.pc$disintegrating()) {
			original.call(this);
		} else {
			this.pc$disintegrateTick();
		}
	}

	@Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
	private void loadDisintegrateTicks(CompoundTag tag, CallbackInfo ci) {
		this.disintegrateTicks = tag.getInt("portalcubed:disintegrate_ticks");
		this.disintegrating = this.disintegrateTicks != 0;
	}

	@Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
	private void saveDisintegrateTicks(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		if (this.pc$disintegrating())
			tag.putInt("portalcubed:disintegrate_ticks", this.disintegrateTicks);
	}

	@ModifyReturnValue(method = "canCollideWith", at = @At("RETURN"))
	private boolean dontCollideIfDisintegrating(boolean original, @Local(ordinal = 1, argsOnly = true) Entity other) {
		return original && !(this.pc$disintegrating() || other.pc$disintegrating());
	}

	@ModifyReturnValue(method = "isNoGravity", at = @At("RETURN"))
	private boolean noGravityIfDisintegrating(boolean original) {
		return original || this.pc$disintegrating();
	}

	@ModifyReturnValue(method = "isSilent", at = @At("RETURN"))
	private boolean silentIfDisintegrating(boolean original) {
		return original || this.pc$disintegrating();
	}

	@ModifyReturnValue(method = "isInvulnerable", at = @At("RETURN"))
	private boolean invulnerableIfDisintegrating(boolean original) {
		return original || this.pc$disintegrating();
	}

	@ModifyReturnValue(method = "isAlive", at = @At("RETURN"))
	private boolean notAliveIfDisintegrating(boolean original) {
		return original && !this.pc$disintegrating();
	}
}
