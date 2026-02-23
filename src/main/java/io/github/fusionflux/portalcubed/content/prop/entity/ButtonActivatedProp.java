package io.github.fusionflux.portalcubed.content.prop.entity;

import java.util.Optional;

import io.github.fusionflux.portalcubed.content.button.ButtonActivated;
import io.github.fusionflux.portalcubed.content.button.FloorButtonBlock;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ButtonActivatedProp extends Prop implements ButtonActivated {
	public static final int ACTIVATION_FLAG_INDEX = 0;
	public static final int DIRTY_FLAG_INDEX = 1;

	private int activatedTimer = 0;

	public ButtonActivatedProp(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
	}

	@SuppressWarnings("SameParameterValue")
	private boolean getVariantFlag(int index) {
		return (this.getVariant() & (1 << index)) != 0;
	}

	private void setVariantFlag(int index, boolean flag) {
		int variant = this.getVariant();
		if (flag) {
			this.setVariant(variant | (1 << index));
		} else {
			this.setVariant(variant & ~(1 << index));
		}
	}

	public void setActivated(boolean activated) {
		if (activated) {
			this.activatedTimer = FloorButtonBlock.PRESSED_TIME;
		}
		this.setVariantFlag(ACTIVATION_FLAG_INDEX, activated);
	}

	@Override
	public Optional<Boolean> isDirty() {
		return Optional.of(this.getVariantFlag(DIRTY_FLAG_INDEX));
	}

	@Override
	public void setDirty(boolean dirty) {
		this.setVariantFlag(DIRTY_FLAG_INDEX, dirty);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.activatedTimer > 0) {
			this.activatedTimer--;
			if (this.activatedTimer == 0) {
				this.setActivated(false);
			}
		}
	}

	@Override
	public void pc$onButtonActivated() {
		this.setActivated(true);
	}

	@Override
	public boolean pc$disintegrate() {
		this.setActivated(false);
		return super.pc$disintegrate();
	}

	@Override
	protected void dropLoot(ServerLevel level, DamageSource source) {
		this.setActivated(false);
		super.dropLoot(level, source);
	}
}
