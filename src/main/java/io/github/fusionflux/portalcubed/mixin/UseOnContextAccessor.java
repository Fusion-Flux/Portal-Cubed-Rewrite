package io.github.fusionflux.portalcubed.mixin;

import net.minecraft.world.item.context.UseOnContext;

import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(UseOnContext.class)
public interface UseOnContextAccessor {
	@Invoker("getHitResult")
	BlockHitResult pc$getHitResult();
}
