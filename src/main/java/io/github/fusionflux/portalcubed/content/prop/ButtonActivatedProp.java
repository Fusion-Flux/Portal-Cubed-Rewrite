package io.github.fusionflux.portalcubed.content.prop;

import io.github.fusionflux.portalcubed.content.button.FloorButtonBlock;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ButtonActivatedProp extends Prop {
	public static final int ACTIVATION_FLAG_INDEX = 0;
	public static final int DIRTY_FLAG_INDEX = 1;

	public ButtonActivatedProp(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
	}

	private boolean getVariantFlag(int index) {
		return (getVariant() & (1 << index)) != 0;
	}

	private void setVariantFlag(int index, boolean flag) {
		int variant = getVariant();
		if (flag) {
			setVariant(variant | (1 << index));
		} else {
			setVariant(variant & ~(1 << index));
		}
	}

	public void setActivated(boolean activated) {
		setVariantFlag(ACTIVATION_FLAG_INDEX, activated);
	}

	@Override
	protected boolean isDirty() {
		return getVariantFlag(DIRTY_FLAG_INDEX);
	}

	@Override
	protected void setDirty(boolean dirty) {
		setVariantFlag(DIRTY_FLAG_INDEX, dirty);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void checkInsideBlocks() {
		var box = this.getBoundingBox();
		var start = BlockPos.containing(box.minX + 1.0E-7, box.minY + 1.0E-7, box.minZ + 1.0E-7);
		var end = BlockPos.containing(box.maxX - 1.0E-7, box.maxY - 1.0E-7, box.maxZ - 1.0E-7);
		if (level().hasChunksAt(start, end) && isAlive()) {
			boolean buttonChecked = false;
			boolean onButton = false;

			for (var pos : BlockPos.betweenClosed(start, end)) {
				var state = level().getBlockState(pos);

				try {
					if (!buttonChecked && state.getBlock() instanceof FloorButtonBlock button) {
						var originPos = button.getOriginPos(pos, state);
						onButton = button.getButtonBounds(state.getValue(FloorButtonBlock.FACING)).move(originPos).intersects(getBoundingBox());
						buttonChecked = true;
					}
					state.entityInside(this.level(), pos, this);
				} catch (Throwable t) {
					var crashReport = CrashReport.forThrowable(t, "Colliding entity with block");
					var crashReportCategory = crashReport.addCategory("Block being collided with");
					CrashReportCategory.populateBlockDetails(crashReportCategory, level(), pos, state);
					throw new ReportedException(crashReport);
				}
			}

			setActivated(onButton);
		}
	}
}
