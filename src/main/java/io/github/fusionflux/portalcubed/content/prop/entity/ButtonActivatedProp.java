package io.github.fusionflux.portalcubed.content.prop.entity;

import java.util.Optional;

import io.github.fusionflux.portalcubed.content.button.FloorButtonBlock;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ButtonActivatedProp extends Prop {
	public static final int ACTIVATION_FLAG_INDEX = 0;
	public static final int DIRTY_FLAG_INDEX = 1;

	private static final double DISINTEGRATION_BUTTON_EJECTION_FORCE = 0.05;

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
	protected Optional<Boolean> isDirty() {
		return Optional.of(getVariantFlag(DIRTY_FLAG_INDEX));
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
	public boolean pc$disintegrate() {
		BlockState feetState = getFeetBlockState();
		if (feetState.getBlock() instanceof FloorButtonBlock)
			setDeltaMovement(Vec3.atLowerCornerOf(feetState.getValue(FloorButtonBlock.FACING).getNormal()).scale(DISINTEGRATION_BUTTON_EJECTION_FORCE));
		setActivated(false);
		return super.pc$disintegrate();
	}

	@Override
	protected void dropLoot(DamageSource source) {
		setActivated(false);
		super.dropLoot(source);
	}
}
