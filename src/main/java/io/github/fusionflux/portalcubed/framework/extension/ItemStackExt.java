package io.github.fusionflux.portalcubed.framework.extension;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public interface ItemStackExt {
	void pc$hurtEquipmentNoUnbreaking(int amount, LivingEntity entity, EquipmentSlot slot);
}
