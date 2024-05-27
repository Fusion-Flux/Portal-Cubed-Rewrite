package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.block.model.multipart.KeyValueCondition;

@Mixin(KeyValueCondition.class)
public interface KeyValueConditionAccessor {
	@Accessor
	String getKey();

	@Accessor
	String getValue();
}
