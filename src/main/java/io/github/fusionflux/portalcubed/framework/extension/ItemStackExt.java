package io.github.fusionflux.portalcubed.framework.extension;

import java.util.function.Consumer;

import net.minecraft.world.entity.LivingEntity;

public interface ItemStackExt {
	<T extends LivingEntity> void pc$hurtAndBreakNoUnbreaking(int amount, T entity, Consumer<T> breakCallback);
}
