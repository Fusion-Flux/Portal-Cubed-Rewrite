package io.github.fusionflux.portalcubed.content.decoration.signage;

import io.github.fusionflux.portalcubed.framework.block.SyncedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class SignageBlockEntity extends SyncedBlockEntity {
	public static final String SIGNAGE_KEY = "signage";

	public final boolean aged;

	protected SignageBlockEntity(BlockEntityType<? extends SignageBlockEntity> type, BlockPos pos, BlockState state, Block aged) {
		super(type, pos, state);
		this.aged = state.is(aged);
	}

	protected final void updateModel() {
		if (this.level != null && this.level.isClientSide) {
			BlockState state = this.getBlockState();
			this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
		}
	}
}
