package io.github.fusionflux.portalcubed.framework.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.PortalValidator;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class PortalValidatorArgumentType implements ArgumentType<PortalValidator.Parsed> {
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_TYPE = new DynamicCommandExceptionType(
			id -> Component.translatableEscape("parsing.portalcubed.portal_validator.not_found", id)
	);

	public static PortalValidatorArgumentType portalValidator() {
		return new PortalValidatorArgumentType();
	}

	public static PortalValidator.Parsed getPortalValidator(CommandContext<?> ctx, String name) {
		return ctx.getArgument(name, PortalValidator.Parsed.class);
	}

	@Override
	public PortalValidator.Parsed parse(StringReader reader) throws CommandSyntaxException {
		ResourceLocation id = ResourceLocation.read(reader);
		PortalValidator.Type<?> type = PortalCubedRegistries.PORTAL_VALIDATOR_TYPE.getValue(id);
		if (type == null) {
			throw ERROR_UNKNOWN_TYPE.createWithContext(reader, id);
		}

		return type.parser().parse(reader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggestResource(PortalCubedRegistries.PORTAL_VALIDATOR_TYPE.keySet(), builder);
	}
}
