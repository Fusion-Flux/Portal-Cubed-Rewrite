package io.github.fusionflux.portalcubed.content.prop.entity;

import java.util.Optional;

import io.github.fusionflux.portalcubed.content.button.FloorButtonBlock;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import net.minecraft.world.damagesource.DamageSource;
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
	public Optional<Boolean> isDirty() {
		return Optional.of(getVariantFlag(DIRTY_FLAG_INDEX));
	}

	@Override
	public void setDirty(boolean dirty) {
		setVariantFlag(DIRTY_FLAG_INDEX, dirty);
	}

	@Override
	public void tick() {
		super.tick();
		if (activatedTimer > 0 && --activatedTimer == 0)
			setActivated(false);
	}

	@Override
	public boolean pc$disintegrate() {
		setActivated(false);
		return super.pc$disintegrate();
	}

	@Override
	protected void dropLoot(DamageSource source) {
		setActivated(false);
		super.dropLoot(source);
	}
}
