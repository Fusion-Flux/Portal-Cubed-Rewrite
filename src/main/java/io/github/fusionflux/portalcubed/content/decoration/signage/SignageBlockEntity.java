package io.github.fusionflux.portalcubed.content.decoration.signage;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class SignageBlockEntity extends BlockEntity {
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

	@Override
	public final Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@NotNull
	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveWithoutMetadata(registries);
	}
}
