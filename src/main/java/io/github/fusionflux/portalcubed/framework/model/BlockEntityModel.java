package io.github.fusionflux.portalcubed.framework.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class BlockEntityModel<T extends BlockEntity> extends HierarchicalModel<BlockEntityModel.EmptyEntity> {
	public abstract void setup(T entity, float tickDelta);

	@Override
	public final void setupAnim(@SuppressWarnings("ClassEscapesDefinedScope") EmptyEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

	}

	protected static final class EmptyEntity extends Entity {
		private EmptyEntity(EntityType<?> variant, Level world) {
			super(variant, world);
		}

		@Override
		protected void defineSynchedData() {

		}

		@Override
		protected void readAdditionalSaveData(CompoundTag nbt) {

		}

		@Override
		protected void addAdditionalSaveData(CompoundTag nbt) {

		}
	}
}
