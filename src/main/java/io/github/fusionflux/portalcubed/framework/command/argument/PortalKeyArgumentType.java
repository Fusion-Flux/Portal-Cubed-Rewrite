package io.github.fusionflux.portalcubed.framework.command.argument;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.github.fusionflux.portalcubed.framework.extension.ClientSuggestionProviderExt;

public class PortalKeyArgumentType implements ArgumentType<String> {
	public static PortalKeyArgumentType portalKey() {
		return new PortalKeyArgumentType();
	}

	public static String getKey(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, String.class);
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		return reader.readString();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		if (!(context.getSource() instanceof ClientSuggestionProviderExt provider))
			return Suggestions.empty();

		String target = provider.pc$getTargetedPortal();
		return target == null ? Suggestions.empty() : builder.suggest(target).buildFuture();
	}
}
