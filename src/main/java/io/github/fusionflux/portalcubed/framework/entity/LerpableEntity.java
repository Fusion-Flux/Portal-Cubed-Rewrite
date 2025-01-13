package io.github.fusionflux.portalcubed.framework.entity;

import io.github.fusionflux.portalcubed.mixin.AbstractBoatAccessor;
import io.github.fusionflux.portalcubed.mixin.LivingEntityAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

// mostly copied from Boat
public abstract class LerpableEntity extends Entity {
	private int lerpSteps;
	private double lerpX;
	private double lerpY;
	private double lerpZ;
	private double lerpYRot;
	private double lerpXRot;

	public LerpableEntity(EntityType<?> variant, Level world) {
		super(variant, world);
	}

	@Override
	public void tick() {
		super.tick();
		this.tickLerp();
	}

	private void tickLerp() {
		if (this.isControlledByLocalInstance()) {
			this.lerpSteps = 0;
		}

		if (this.lerpSteps > 0) {
			this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
			this.lerpSteps--;
		}
	}

	@Override
	public void lerpTo(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
		this.lerpX = x;
		this.lerpY = y;
		this.lerpZ = z;
		this.lerpYRot = yaw;
		this.lerpXRot = pitch;
		this.lerpSteps = 3;
	}

	@Override
	public double lerpTargetX() {
		return this.lerpSteps > 0 ? this.lerpX : this.getX();
	}

	@Override
	public double lerpTargetY() {
		return this.lerpSteps > 0 ? this.lerpY : this.getY();
	}

	@Override
	public double lerpTargetZ() {
		return this.lerpSteps > 0 ? this.lerpZ : this.getZ();
	}

	@Override
	public float lerpTargetXRot() {
		return this.lerpSteps > 0 ? (float)this.lerpXRot : this.getXRot();
	}

	@Override
	public float lerpTargetYRot() {
		return this.lerpSteps > 0 ? (float)this.lerpYRot : this.getYRot();
	}

	public static int getLerpSteps(Entity entity) {
		if (entity instanceof LivingEntityAccessor living) {
			return living.getLerpSteps();
		} else if (entity instanceof LerpableEntity lerpable) {
			return lerpable.lerpSteps;
		} else if (entity instanceof AbstractBoatAccessor boat) {
			return boat.getLerpSteps();
//		} else if (entity instanceof AbstractMinecartAccessor minecart) {
//			return minecart.getLerpSteps();
		} else {
			return 0;
		}
	}

	public static void setLerpSteps(Entity entity, int steps) {
		if (entity instanceof LivingEntityAccessor living) {
			living.setLerpSteps(steps);
		} else if (entity instanceof LerpableEntity lerpable) {
			lerpable.lerpSteps = steps;
		} else if (entity instanceof AbstractBoatAccessor boat) {
			boat.setLerpSteps(steps);
		}
//		} else if (entity instanceof AbstractMinecartAccessor minecart) {
//			minecart.setLerpSteps(steps);
//		}
	}
}
