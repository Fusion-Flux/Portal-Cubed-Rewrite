package io.github.fusionflux.portalcubed.content;

import com.mojang.brigadier.suggestion.SuggestionProvider;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.command.SuggestionAdder;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalKeyArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;

public class PortalCubedSuggestionProviders {
	public static final SuggestionProvider<CommandSourceStack> PORTAL_CREATION_KEYS = registerAdder(
			"portal_creation_keys", (ctx, builder) -> {
				SharedSuggestionProvider source = ctx.getSource();
				PortalCubedCommands.suggest(source.getOnlinePlayerNames(), builder, PortalKeyArgumentType.ONLINE_PLAYER);
				PortalCubedCommands.suggest(source.getSelectedEntities(), builder, PortalKeyArgumentType.TARGETED_ENTITY);
				return builder.buildFuture();
			}
	);

	private static <S extends SharedSuggestionProvider> SuggestionProvider<S> registerAdder(String name, SuggestionAdder<SharedSuggestionProvider> provider) {
		return register(name, provider);
	}

	private static <S extends SharedSuggestionProvider> SuggestionProvider<S> register(String name, SuggestionProvider<SharedSuggestionProvider> provider) {
		return SuggestionProviders.register(PortalCubed.id(name), provider);
	}
}
