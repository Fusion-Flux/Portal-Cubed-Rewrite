package io.github.fusionflux.portalcubed.framework.command;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

/**
 * Marker interface that indicates that provided suggestions should only be added,
 * instead of replacing all suggestions provided by default by the argument type.
 */
public interface SuggestionAdder<S> extends SuggestionProvider<S> {
	static <S> SuggestionAdder<S> wrap(SuggestionProvider<S> provider) {
		return new Wrapper<>(provider);
	}

	class Wrapper<S> implements SuggestionAdder<S> {
		private final SuggestionProvider<S> wrapped;

		public Wrapper(SuggestionProvider<S> wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) throws CommandSyntaxException {
			return this.wrapped.getSuggestions(context, builder);
		}
	}
}
