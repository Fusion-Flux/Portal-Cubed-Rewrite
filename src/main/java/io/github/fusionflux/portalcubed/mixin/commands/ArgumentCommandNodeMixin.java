package io.github.fusionflux.portalcubed.mixin.commands;

import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;

import io.github.fusionflux.portalcubed.framework.command.SuggestionAdder;

@Mixin(value = ArgumentCommandNode.class, remap = false)
public class ArgumentCommandNodeMixin<S, T> {
	@Shadow
	@Final
	@Mutable
	private SuggestionProvider<S> customSuggestions;

	@WrapMethod(method = "listSuggestions")
	private CompletableFuture<Suggestions> handleSuggestionAdders(CommandContext<S> context, SuggestionsBuilder builder, Operation<CompletableFuture<Suggestions>> original) {
		CompletableFuture<Suggestions> base = original.call(context, builder);

		if (this.customSuggestions instanceof SuggestionAdder<S> adder) {
			// call the method again, this time pretending there's no custom suggestions
			try {
				this.customSuggestions = null;
				return original.call(context, builder);
			} finally {
				this.customSuggestions = adder;
			}
		} else {
			return base;
		}
	}
}
