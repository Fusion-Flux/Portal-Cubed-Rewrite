package io.github.fusionflux.portalcubed.content.prop;

import io.github.fusionflux.portalcubed.content.button.FloorButtonBlock;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ButtonActivatedProp extends Prop {
	public static final int ACTIVATION_FLAG_INDEX = 0;
	public static final int DIRTY_FLAG_INDEX = 1;

	private int activatedTimer = 0;

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
		if (activated)
			activatedTimer = FloorButtonBlock.PRESSED_TIME;
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

	@Override
	public void tick() {
		super.tick();
		if (activatedTimer > 0 && --activatedTimer == 0)
			setActivated(false);
	}

	@Override
	protected void dropLoot() {
		setActivated(false);
		super.dropLoot();
	}
}
