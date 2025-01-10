package io.github.fusionflux.portalcubed.content.button.pedestal;

import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import io.github.fusionflux.portalcubed.framework.block.SyncedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class PedestalButtonBlockEntity extends SyncedBlockEntity {
	public static final int DEFAULT_PRESS_TIME = 20 * 3;
	public static final int MIN_PRESS_TIME = 20;
	public static final int MAX_PRESS_TIME = 20 * 99;

	private int pressTime = DEFAULT_PRESS_TIME;

	public PedestalButtonBlockEntity(BlockPos pos, BlockState state) {
		super(PortalCubedBlockEntityTypes.PEDESTAL_BUTTON, pos, state);
	}

	public void setPressTime(int pressTime) {
		if (this.pressTime != pressTime) {
			this.pressTime = Mth.clamp(pressTime, MIN_PRESS_TIME, MAX_PRESS_TIME);
			this.setChangedAndSync();
		}
	}

	public int getPressTime() {
		return this.pressTime;
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		this.setPressTime(tag.getInt("press_time"));
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		tag.putInt("press_time", this.pressTime);
	}
}
