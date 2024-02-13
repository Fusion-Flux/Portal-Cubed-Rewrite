package io.github.fusionflux.portalcubed.content.prop;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class P1CompanionCube extends Prop {
	public P1CompanionCube(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
	}

	@Override
	public void lavaHurt() {
		super.lavaHurt();
		setDirty(true);
	}

	@Override
	public void setRemainingFireTicks(int ticks) {
		super.setRemainingFireTicks(ticks);
		if (getRemainingFireTicks() > 0) setDirty(true);
	}
}
