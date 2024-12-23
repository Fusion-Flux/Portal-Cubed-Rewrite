package io.github.fusionflux.portalcubed.framework.command.argument;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.ApiStatus;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.github.fusionflux.portalcubed.content.PortalCubedCommands;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;

public class FlagArgumentType implements ArgumentType<Unit> {
	private final String name;
	private final SimpleCommandExceptionType expectedValue;
	private final DynamicCommandExceptionType invalidValue;

	public FlagArgumentType(String name) {
		this.name = name;
		this.expectedValue = new SimpleCommandExceptionType(lang("expected"));
		this.invalidValue = new DynamicCommandExceptionType(string -> lang("invalid", string));
	}

	private Component lang(String key, Object... args) {
		return Component.translatable("parsing.portalcubed.flag." + this.name + '.' + key, args);
	}

	/**
	 * Shouldn't be used directly, use {@link PortalCubedCommands#flag(String)}
	 */
	@ApiStatus.Internal
	public static FlagArgumentType flag(String name) {
		return new FlagArgumentType(name);
	}

	/**
	 * Shouldn't be used directly, use {@link PortalCubedCommands#getFlag(CommandContext, String)}
	 */
	@ApiStatus.Internal
	public static boolean getFlag(CommandContext<?> ctx, String name) {
		return PortalCubedCommands.hasArgument(ctx, name);
	}

	@Override
	public Unit parse(StringReader reader) throws CommandSyntaxException {
		int cursor = reader.getCursor();
		String string = reader.readString();
		if (string.isEmpty()) {
			throw this.expectedValue.createWithContext(reader);
		}

		if (string.equals(this.name)) {
			return Unit.INSTANCE;
		}

		reader.setCursor(cursor);
		throw this.invalidValue.createWithContext(reader, string);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		String remaining = builder.getRemainingLowerCase();
		if (this.name.startsWith(remaining)) {
			builder.suggest(this.name);
			return builder.buildFuture();
		} else {
			return Suggestions.empty();
		}
	}

	@Override
	public Collection<String> getExamples() {
		return List.of(this.name);
	}

	public static class Serializer implements ArgumentTypeInfo<FlagArgumentType, Serializer.Template> {
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public void serializeToNetwork(Template template, FriendlyByteBuf buf) {
			buf.writeUtf(template.name);
		}

		@Override
		public Template deserializeFromNetwork(FriendlyByteBuf buf) {
			return new Template(buf.readUtf());
		}

		@Override
		public void serializeToJson(Template template, JsonObject json) {
			json.addProperty("name", template.name);
		}

		@Override
		public Template unpack(FlagArgumentType type) {
			return new Template(type.name);
		}

		public final class Template implements ArgumentTypeInfo.Template<FlagArgumentType> {
			final String name;

			public Template(String name) {
				this.name = name;
			}

			public FlagArgumentType instantiate(CommandBuildContext ctx) {
				return flag(this.name);
			}

			@Override
			public ArgumentTypeInfo<FlagArgumentType, ?> type() {
				return Serializer.this;
			}
		}
	}
}
