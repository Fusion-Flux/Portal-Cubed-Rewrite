package io.github.fusionflux.portalcubed.content.decoration.signage;

import io.github.fusionflux.portalcubed.framework.block.SyncedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class SignageBlockEntity extends SyncedBlockEntity {
	public final boolean aged;

	protected SignageBlockEntity(BlockEntityType<? extends SignageBlockEntity> type, BlockPos pos, BlockState state, Block aged) {
		super(type, pos, state);
		this.aged = state.is(aged);
	}

	protected final void updateImage() {
		Level level = this.getLevel();
		if (level != null) {
			if (level.isClientSide) {
				BlockState state = this.getBlockState();
				level.sendBlockUpdated(this.getBlockPos(), state, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
			} else {
				this.setChangedAndSync();
			}
		}
	}
}
