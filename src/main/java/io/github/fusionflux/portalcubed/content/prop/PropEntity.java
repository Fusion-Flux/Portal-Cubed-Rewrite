package io.github.fusionflux.portalcubed.content.prop;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class PropEntity extends Entity {
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(PropEntity.class, EntityDataSerializers.INT);

	public final PropType type;

	public PropEntity(PropType type, EntityType<?> entityType, Level level) {
		super(entityType, level);
		this.type = type;
	}

	public int getVariant() {
		return entityData.get(VARIANT);
	}

	public void setVariant(int variant) {
		entityData.set(VARIANT, variant);
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(VARIANT, 0);
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
