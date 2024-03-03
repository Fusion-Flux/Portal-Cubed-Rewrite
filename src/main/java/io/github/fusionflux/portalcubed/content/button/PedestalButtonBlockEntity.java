package io.github.fusionflux.portalcubed.content.button;

import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PedestalButtonBlockEntity extends BlockEntity {
	public static final int DEFAULT_PRESS_TIME = 20 * 3;

	private int pressTime = DEFAULT_PRESS_TIME;

	public PedestalButtonBlockEntity(BlockPos pos, BlockState state) {
		super(PortalCubedBlockEntityTypes.PEDESTAL_BUTTON, pos, state);
	}

	public boolean setPressTime(int pressTime) {
		boolean changed = this.pressTime != pressTime;
		this.pressTime = pressTime;
		if (changed) setChanged();
		return changed;
	}

	public int getPressTime() {
		return pressTime;
	}

	@Override
	public void load(CompoundTag nbt) {
		pressTime = nbt.getInt("press_time");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt) {
		nbt.putInt("press_time", pressTime);
	}
}
