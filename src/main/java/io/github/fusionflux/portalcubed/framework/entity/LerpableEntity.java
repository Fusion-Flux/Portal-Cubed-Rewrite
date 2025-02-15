package io.github.fusionflux.portalcubed.framework.entity;

import io.github.fusionflux.portalcubed.mixin.utils.accessors.AbstractBoatAccessor;
import io.github.fusionflux.portalcubed.mixin.utils.accessors.LivingEntityAccessor;
import io.github.fusionflux.portalcubed.mixin.utils.accessors.OldMinecartBehaviorAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;

// mostly copied from Boat
public abstract class LerpableEntity extends Entity {
	private int lerpSteps;
	private double lerpX;
	private double lerpY;
	private double lerpZ;
	private double lerpYRot;
	private double lerpXRot;

	protected LerpableEntity(EntityType<?> variant, Level world) {
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
	public void cancelLerp() {
		this.lerpSteps = 0;
	}

	@Override
	public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps) {
		this.lerpX = x;
		this.lerpY = y;
		this.lerpZ = z;
		this.lerpYRot = yaw;
		this.lerpXRot = pitch;
		this.lerpSteps = steps;
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
		return switch (entity) {
			case LivingEntityAccessor living -> living.getLerpSteps();
			case LerpableEntity lerpable -> lerpable.lerpSteps;
			case AbstractBoatAccessor boat -> boat.getLerpSteps();
			case AbstractMinecart minecart -> {
				if (minecart.getBehavior() instanceof OldMinecartBehaviorAccessor oldBehavior) {
					yield oldBehavior.getLerpSteps();
				} else {
					throw new RuntimeException("New minecart behavior is weird");
				}
			}
			default -> 1;
		};
	}

	public static void setLerpSteps(Entity entity, int steps) {
		switch (entity) {
			case LivingEntityAccessor living -> living.setLerpSteps(steps);
			case LerpableEntity lerpable -> lerpable.lerpSteps = steps;
			case AbstractBoatAccessor boat -> boat.setLerpSteps(steps);
			case AbstractMinecart minecart -> {
				if (minecart.getBehavior() instanceof OldMinecartBehaviorAccessor oldBehavior) {
					oldBehavior.setLerpSteps(steps);
				} else {
					throw new RuntimeException("New minecart behavior is weird");
				}
			}
			default -> {}
		}
	}
}
