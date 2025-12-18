package io.github.fusionflux.portalcubed.content.portal.command.argument.portal;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemArgument;

/**
 * This whole group of classes is based on {@link ItemArgument}.
 */
public final class PortalArgument implements ArgumentType<PortalInput> {
	private final PortalParser parser;

	private PortalArgument(CommandBuildContext context) {
		this.parser = new PortalParser(context);
	}

	public static PortalArgument portal(CommandBuildContext context) {
		return new PortalArgument(context);
	}

	public static <S> PortalInput getPortal(CommandContext<S> context, String name) {
		return context.getArgument(name, PortalInput.class);
	}

	@Override
	public PortalInput parse(StringReader reader) throws CommandSyntaxException {
		PortalParser.PortalResult result = this.parser.parse(reader);
		return new PortalInput(result.type(), result.attributes());
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return this.parser.fillSuggestions(builder);
	}
}
