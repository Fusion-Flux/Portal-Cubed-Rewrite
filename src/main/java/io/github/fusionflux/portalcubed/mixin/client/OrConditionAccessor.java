package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.client.renderer.block.model.multipart.OrCondition;

@Mixin(OrCondition.class)
public interface OrConditionAccessor {
	@Accessor
	Iterable<? extends Condition> getConditions();
}
