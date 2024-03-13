package io.github.fusionflux.portalcubed.content.button.pedestal;

import org.quiltmc.qsl.block.entity.api.QuiltBlockEntity;

import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PedestalButtonBlockEntity extends BlockEntity implements QuiltBlockEntity {
	public static final int DEFAULT_PRESS_TIME = 20 * 3;
	public static final IntIntPair PRESS_TIME_RANGE = IntIntPair.of(20 * 1, 20 * 99);

	private int pressTime = DEFAULT_PRESS_TIME;

	public PedestalButtonBlockEntity(BlockPos pos, BlockState state) {
		super(PortalCubedBlockEntityTypes.PEDESTAL_BUTTON, pos, state);
	}

	public boolean setPressTime(int pressTime) {
		boolean changed = this.pressTime != pressTime;
		this.pressTime = Mth.clamp(pressTime, PRESS_TIME_RANGE.leftInt(), PRESS_TIME_RANGE.rightInt());
		if (level instanceof ServerLevel && changed) {
			sync();
			level.blockEntityChanged(worldPosition);
		}
		return changed;
	}

	public int getPressTime() {
		return pressTime;
	}

	@Override
	public void load(CompoundTag nbt) {
		setPressTime(nbt.getInt("press_time"));
	}

	@Override
	protected void saveAdditional(CompoundTag nbt) {
		nbt.putInt("press_time", pressTime);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return saveWithoutMetadata();
	}
}
