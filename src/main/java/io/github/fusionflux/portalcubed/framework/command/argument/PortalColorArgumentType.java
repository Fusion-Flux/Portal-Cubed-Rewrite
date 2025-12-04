package io.github.fusionflux.portalcubed.framework.command.argument;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.github.fusionflux.portalcubed.content.portal.graphics.color.PortalColor;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class PortalColorArgumentType implements ArgumentType<PortalColor> {
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_TYPE = new DynamicCommandExceptionType(
			id -> Component.translatableEscape("parsing.portalcubed.portal_color.not_found", id)
	);

	private final Map<String, PortalColor.Type> types;

	public PortalColorArgumentType() {
		this.types = new LinkedHashMap<>();
		for (PortalColor.Type type : PortalColor.Type.values()) {
			this.types.put(type.name, type);
		}
	}

	public static PortalColorArgumentType portalColor() {
		return new PortalColorArgumentType();
	}

	public static PortalColor getPortalColor(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, PortalColor.class);
	}

	@Override
	public PortalColor parse(StringReader reader) throws CommandSyntaxException {
		String name = reader.readString();
		PortalColor.Type type = this.types.get(name);
		if (type == null) {
			throw ERROR_UNKNOWN_TYPE.createWithContext(reader, name);
		}

		return type.parser.parse(reader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(this.types.keySet(), builder);
	}
}
