package io.github.fusionflux.portalcubed.framework;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class UnsavedEntity extends Entity {
	public UnsavedEntity(EntityType<?> variant, Level world) {
		super(variant, world);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt) {
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
	}
}
