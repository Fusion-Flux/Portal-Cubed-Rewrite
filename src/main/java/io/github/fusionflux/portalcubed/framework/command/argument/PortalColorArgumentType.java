package io.github.fusionflux.portalcubed.framework.command.argument;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.content.portal.color.PortalColor;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PortalColorArgumentType implements ArgumentType<PortalColor> {
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_TYPE = new DynamicCommandExceptionType(
			id -> Component.translatableEscape("parsing.portalcubed.portal_color.not_found", id)
	);

	public static PortalColorArgumentType portalColor() {
		return new PortalColorArgumentType();
	}

	public static PortalColor getPortalColor(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, PortalColor.class);
	}

	@Override
	public PortalColor parse(StringReader reader) throws CommandSyntaxException {
		ResourceLocation id = ResourceLocation.read(reader);
		PortalColor.Type<?> type = PortalCubedRegistries.PORTAL_COLOR_TYPE.getValue(id);
		if (type == null) {
			throw ERROR_UNKNOWN_TYPE.createWithContext(reader, id);
		}

		return type.parser().parse(reader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggestResource(PortalCubedRegistries.PORTAL_COLOR_TYPE.keySet(), builder);
	}
}
