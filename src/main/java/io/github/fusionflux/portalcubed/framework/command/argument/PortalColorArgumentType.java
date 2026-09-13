package io.github.fusionflux.portalcubed.framework.command.argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.github.fusionflux.portalcubed.content.portal.graphics.color.JebPortalColor;
import io.github.fusionflux.portalcubed.content.portal.graphics.color.PortalColor;
import io.github.fusionflux.portalcubed.mixin.portals.TextColorAccessor;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.util.Util;

public class PortalColorArgumentType implements ArgumentType<PortalColor> {
	public static final List<String> SUGGESTIONS = Util.make(() -> {
		List<String> list = new ArrayList<>(TextColorAccessor.getNAMED_COLORS().keySet());
		list.add("#"); // start of a hex color
		list.add(JebPortalColor.PREFIX);
		return List.copyOf(list);
	});

	public static PortalColorArgumentType portalColor() {
		return new PortalColorArgumentType();
	}

	public static PortalColor getPortalColor(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, PortalColor.class);
	}

	@Override
	public PortalColor parse(StringReader reader) throws CommandSyntaxException {
		return PortalColor.parse(reader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(SUGGESTIONS, builder);
	}

	@Override
	public Collection<String> getExamples() {
		return SUGGESTIONS;
	}
}
