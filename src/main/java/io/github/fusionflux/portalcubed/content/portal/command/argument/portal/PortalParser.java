package io.github.fusionflux.portalcubed.content.portal.command.argument.portal;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableObject;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import io.github.fusionflux.portalcubed.content.PortalCubedCommands;
import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Heavily modified copy of vanilla's {@link ItemParser} that works with {@link PortalType}s
 * and {@link PortalAttribute}s instead of {@link Item}s and {@link DataComponentType}s.
 */
public final class PortalParser {
	static final DynamicCommandExceptionType ERROR_UNKNOWN_PORTAL_TYPE = new DynamicCommandExceptionType(
			object -> Component.translatableEscape("argument.portalcubed.portal.type.invalid", object)
	);
	static final DynamicCommandExceptionType ERROR_UNKNOWN_ATTRIBUTE = new DynamicCommandExceptionType(
			object -> Component.translatableEscape("arguments.portalcubed.portal.attribute.unknown", object)
	);
	static final Dynamic2CommandExceptionType ERROR_MALFORMED_ATTRIBUTE = new Dynamic2CommandExceptionType(
			(object, object2) -> Component.translatableEscape("arguments.portalcubed.portal.attribute.malformed", object, object2)
	);
	static final SimpleCommandExceptionType ERROR_EXPECTED_ATTRIBUTE = new SimpleCommandExceptionType(
			Component.translatable("arguments.portalcubed.portal.attribute.expected")
	);
	static final DynamicCommandExceptionType ERROR_REPEATED_ATTRIBUTE = new DynamicCommandExceptionType(
			object -> Component.translatableEscape("arguments.portalcubed.portal.attribute.repeated", object)
	);
	private static final SimpleCommandExceptionType ERROR_MALFORMED_PORTAL_RENDERING = new SimpleCommandExceptionType(
			Component.translatableEscape("arguments.portalcubed.portal.malformed.rendering")
	);
	public static final char SYNTAX_START_COMPONENTS = '[';
	public static final char SYNTAX_END_COMPONENTS = ']';
	public static final char SYNTAX_COMPONENT_SEPARATOR = ',';
	public static final char SYNTAX_COMPONENT_ASSIGNMENT = '=';
	static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;

	private final HolderLookup.RegistryLookup<PortalType> portalTypes;
	private final DynamicOps<Tag> registryOps;

	public PortalParser(HolderLookup.Provider registries) {
		this.portalTypes = registries.lookupOrThrow(PortalCubedRegistries.PORTAL_TYPE);
		this.registryOps = registries.createSerializationContext(NbtOps.INSTANCE);
	}


	public PortalResult parse(StringReader reader) throws CommandSyntaxException {
		MutableObject<Holder<PortalType>> portalType = new MutableObject<>();
		PortalAttributeMap attributes = new PortalAttributeMap();

		this.parse(reader, new Visitor() {
			@Override
			public void visitType(Holder<PortalType> type) {
				portalType.setValue(type);
			}

			@Override
			public <T> void visitAttribute(PortalAttribute<T> attribute, T value) {
				attributes.put(attribute, value);
			}
		});

		Holder<PortalType> holder = Objects.requireNonNull(portalType.getValue(), "Parser gave no portal type");
		validateAttributes(reader, holder, attributes);
		return new PortalResult(holder, attributes);
	}

	private static void validateAttributes(StringReader reader, Holder<PortalType> type, PortalAttributeMap attributes) throws CommandSyntaxException {
		if (type.value().stencil().isEmpty() && attributes.containsKey(PortalAttribute.NO_RENDERING)) {
			throw ERROR_MALFORMED_PORTAL_RENDERING.createWithContext(reader);
		}
	}

	public void parse(StringReader reader, Visitor visitor) throws CommandSyntaxException {
		int cursor = reader.getCursor();

		try {
			new State(reader, visitor).parse();
		} catch (CommandSyntaxException e) {
			reader.setCursor(cursor);
			throw e;
		}
	}

	public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder builder) {
		StringReader reader = new StringReader(builder.getInput());
		reader.setCursor(builder.getStart());
		SuggestionsVisitor visitor = new SuggestionsVisitor();
		State state = new State(reader, visitor);

		try {
			state.parse();
		} catch (CommandSyntaxException ignored) {
		}

		return visitor.resolveSuggestions(builder, reader);
	}

	public record PortalResult(Holder<PortalType> type, PortalAttributeMap attributes) {
	}

	class State {
		private final StringReader reader;
		private final Visitor visitor;

		State(StringReader reader, Visitor visitor) {
			this.reader = reader;
			this.visitor = visitor;
		}

		public void parse() throws CommandSyntaxException {
			this.visitor.visitSuggestions(this::suggestPortalType);
			this.readPortalType();
			this.visitor.visitSuggestions(this::suggestStartAttributes);
			if (this.reader.canRead() && this.reader.peek() == SYNTAX_START_COMPONENTS) {
				this.visitor.visitSuggestions(SUGGEST_NOTHING);
				this.readAttributes();
			}
		}

		private void readPortalType() throws CommandSyntaxException {
			int cursor = this.reader.getCursor();
			ResourceLocation id = ResourceLocation.read(this.reader);
			ResourceKey<PortalType> key = ResourceKey.create(PortalCubedRegistries.PORTAL_TYPE, id);

			this.visitor.visitType(PortalParser.this.portalTypes.get(key).orElseThrow(() -> {
				this.reader.setCursor(cursor);
				return ERROR_UNKNOWN_PORTAL_TYPE.createWithContext(this.reader, id);
			}));
		}

		private void readAttributes() throws CommandSyntaxException {
			this.reader.expect(SYNTAX_START_COMPONENTS);
			this.visitor.visitSuggestions(this::suggestAttributeAssignment);
			Set<PortalAttribute<?>> set = new ReferenceArraySet<>();

			while (this.reader.canRead() && this.reader.peek() != SYNTAX_END_COMPONENTS) {
				this.reader.skipWhitespace();
				PortalAttribute<?> attribute = readPortalAttribute(this.reader);
				if (!set.add(attribute)) {
					throw ERROR_REPEATED_ATTRIBUTE.create(attribute);
				}

				this.visitor.visitSuggestions(this::suggestAssignment);
				this.reader.skipWhitespace();
				this.reader.expect(SYNTAX_COMPONENT_ASSIGNMENT);
				this.visitor.visitSuggestions(SUGGEST_NOTHING);
				this.reader.skipWhitespace();
				this.readAttributeValue(attribute);
				this.reader.skipWhitespace();

				this.visitor.visitSuggestions(this::suggestNextOrEndAttributes);
				if (!this.reader.canRead() || this.reader.peek() != SYNTAX_COMPONENT_SEPARATOR) {
					break;
				}

				this.reader.skip();
				this.reader.skipWhitespace();
				this.visitor.visitSuggestions(this::suggestAttributeAssignment);
				if (!this.reader.canRead()) {
					throw ERROR_EXPECTED_ATTRIBUTE.createWithContext(this.reader);
				}
			}

			this.reader.expect(SYNTAX_END_COMPONENTS);
			this.visitor.visitSuggestions(SUGGEST_NOTHING);
		}

		public static PortalAttribute<?> readPortalAttribute(StringReader reader) throws CommandSyntaxException {
			if (!reader.canRead()) {
				throw ERROR_EXPECTED_ATTRIBUTE.createWithContext(reader);
			}

			int cursor = reader.getCursor();
			String name = reader.readString();
			PortalAttribute<?> attribute = PortalAttribute.REGISTRY.get(name);
			if (attribute != null) {
				return attribute;
			}

			reader.setCursor(cursor);
			throw ERROR_UNKNOWN_ATTRIBUTE.createWithContext(reader, name);
		}

		private <T> void readAttributeValue(PortalAttribute<T> attribute) throws CommandSyntaxException {
			int cursor = this.reader.getCursor();
			Tag tag = new TagParser(this.reader).readValue();
			DataResult<T> dataResult = attribute.codec.parse(PortalParser.this.registryOps, tag);
			this.visitor.visitAttribute(attribute, dataResult.getOrThrow(error -> {
				this.reader.setCursor(cursor);
				return ERROR_MALFORMED_ATTRIBUTE.createWithContext(this.reader, attribute.name, error);
			}));
		}

		private CompletableFuture<Suggestions> suggestStartAttributes(SuggestionsBuilder builder) {
			if (builder.getRemaining().isEmpty()) {
				builder.suggest(String.valueOf(SYNTAX_START_COMPONENTS));
			}

			return builder.buildFuture();
		}

		private CompletableFuture<Suggestions> suggestNextOrEndAttributes(SuggestionsBuilder builder) {
			if (builder.getRemaining().isEmpty()) {
				builder.suggest(String.valueOf(SYNTAX_COMPONENT_SEPARATOR));
				builder.suggest(String.valueOf(SYNTAX_END_COMPONENTS));
			}

			return builder.buildFuture();
		}

		private CompletableFuture<Suggestions> suggestAssignment(SuggestionsBuilder builder) {
			if (builder.getRemaining().isEmpty()) {
				builder.suggest(String.valueOf(SYNTAX_COMPONENT_ASSIGNMENT));
			}

			return builder.buildFuture();
		}

		private CompletableFuture<Suggestions> suggestPortalType(SuggestionsBuilder builder) {
			return SharedSuggestionProvider.suggestResource(PortalParser.this.portalTypes.listElementIds().map(ResourceKey::location), builder);
		}

		private CompletableFuture<Suggestions> suggestAttributeAssignment(SuggestionsBuilder builder) {
			return this.suggestAttribute(builder, String.valueOf(SYNTAX_COMPONENT_ASSIGNMENT));
		}

		private CompletableFuture<Suggestions> suggestAttribute(SuggestionsBuilder builder, String suffix) {
			String command = builder.getRemaining().toLowerCase(Locale.ROOT);

			PortalCubedCommands.filterResources(
					PortalAttribute.REGISTRY.values(), command,
					attribute -> attribute.name,
					attribute -> builder.suggest(attribute.name + suffix)
			);

			return builder.buildFuture();
		}
	}

	static class SuggestionsVisitor implements Visitor {
		private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

		@Override
		public void visitSuggestions(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions) {
			this.suggestions = suggestions;
		}

		public CompletableFuture<Suggestions> resolveSuggestions(SuggestionsBuilder builder, StringReader reader) {
			return this.suggestions.apply(builder.createOffset(reader.getCursor()));
		}
	}

	public interface Visitor {
		default void visitType(Holder<PortalType> type) {
		}

		default <T> void visitAttribute(PortalAttribute<T> attribute, T value) {
		}

		default void visitSuggestions(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions) {
		}
	}
}
