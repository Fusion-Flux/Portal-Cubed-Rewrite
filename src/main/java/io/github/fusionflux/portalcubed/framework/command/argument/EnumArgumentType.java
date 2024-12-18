package io.github.fusionflux.portalcubed.framework.command.argument;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public abstract class EnumArgumentType<E extends Enum<E>> implements ArgumentType<E> {
	private final String name;
	private final Map<String, E> values;
	private final SimpleCommandExceptionType expectedValue;
	private final DynamicCommandExceptionType invalidValue;

	protected EnumArgumentType(Class<E> clazz) {
		this(clazz, Namer.LOWERCASE_ENUM_NAME);
	}

	protected EnumArgumentType(Class<E> clazz, Namer<? super E> namer) {
		this.name = clazz.getSimpleName();
		this.values = new LinkedHashMap<>();
		for (E value : clazz.getEnumConstants()) {
			this.values.put(namer.getName(value), value);
		}

		this.expectedValue = new SimpleCommandExceptionType(lang("expected"));
		this.invalidValue = new DynamicCommandExceptionType(string -> lang("invalid", string));
	}

	private Component lang(String suffix, Object... args) {
		return Component.translatable("parsing.portalcubed.enum." + this.name + '.' + suffix, args);
	}

	@Override
	public E parse(StringReader reader) throws CommandSyntaxException {
		// based on readBoolean
		int start = reader.getCursor();
		String value = reader.readString();
		if (value.isEmpty()) {
			throw this.expectedValue.createWithContext(reader);
		}

		for (Map.Entry<String, E> entry : this.values.entrySet()) {
			if (value.equals(entry.getKey())) {
				return entry.getValue();
			}
		}

		reader.setCursor(start);
		throw this.invalidValue.createWithContext(reader, value);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		String remaining = builder.getRemainingLowerCase();
		for (String key : this.values.keySet()) {
			if (key.startsWith(remaining)) {
				builder.suggest(key);
			}
		}
		return builder.buildFuture();
	}

	@Override
	public Collection<String> getExamples() {
		return this.values.keySet();
	}

	public interface Namer<E> {
		Namer<StringRepresentable> STRING_REPR = StringRepresentable::getSerializedName;
		Namer<Enum<?>> LOWERCASE_ENUM_NAME = value -> value.name().toLowerCase(Locale.ROOT);

		String getName(E value);
	}
}
