package io.github.fusionflux.portalcubed.framework.command.argument;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalId;

public final class PortalIdArgumentType implements ArgumentType<PortalId> {
	private static final PortalKeyArgumentType keyType = PortalKeyArgumentType.portalKey();
	private static final PolarityArgumentType polarityType = PolarityArgumentType.polarity();

	public static PortalIdArgumentType portalId() {
		return new PortalIdArgumentType();
	}

	public static PortalId getId(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, PortalId.class);
	}

	@Override
	public PortalId parse(StringReader reader) throws CommandSyntaxException {
		String key = keyType.parse(reader);
		reader.expect(' ');
		Polarity polarity = polarityType.parse(reader);
		return new PortalId(key, polarity);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		// TODO: implement this once I can debug it
		return ArgumentType.super.listSuggestions(context, builder);
	}
}
