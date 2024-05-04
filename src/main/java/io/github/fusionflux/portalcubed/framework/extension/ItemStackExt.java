package io.github.fusionflux.portalcubed.framework.extension;

import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;

public interface ItemStackExt {
	<T extends LivingEntity> void pc$hurtAndBreakNoUnbreaking(int amount, T entity, Consumer<T> breakCallback);
}
