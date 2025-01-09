package io.github.fusionflux.portalcubed.framework.command.argument;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.github.fusionflux.portalcubed.content.PortalCubedCommands;
import io.github.fusionflux.portalcubed.framework.extension.ClientSuggestionProviderExt;
import net.minecraft.network.chat.Component;

public class PortalKeyArgumentType implements ArgumentType<String> {
	public static final Component TARGETED_PORTAL = lang("targeted_portal");
	public static final Component TARGETED_ENTITY = lang("targeted_entity");
	public static final Component EXISTING_KEY = lang("existing_key");
	public static final Component ONLINE_PLAYER = lang("online_player");

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
		if (target != null) {
			PortalCubedCommands.suggest(List.of(target), builder, TARGETED_PORTAL);
		}

		List<String> keys = provider.pc$getAllPortalKeys().stream()
				.filter(key -> !Objects.equals(target, key))
				.toList();
		PortalCubedCommands.suggest(keys, builder, EXISTING_KEY);
		return builder.buildFuture();
	}

	private static Component lang(String key) {
		return Component.translatable("argument.portalcubed.portal_key." + key);
	}
}
