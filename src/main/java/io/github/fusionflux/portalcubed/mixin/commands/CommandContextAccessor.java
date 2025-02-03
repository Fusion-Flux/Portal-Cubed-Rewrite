package io.github.fusionflux.portalcubed.mixin.commands;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;

@Mixin(value = CommandContext.class, remap = false)
public interface CommandContextAccessor<S> {
	@Accessor
	Map<String, ParsedArgument<S, ?>> getArguments();
}
