package io.github.fusionflux.portalcubed.content;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import com.mojang.brigadier.context.CommandContext;

import com.mojang.brigadier.context.ParsedArgument;

import io.github.fusionflux.portalcubed.PortalCubed;

import io.github.fusionflux.portalcubed.content.command.CreateConstructCommand;

import io.github.fusionflux.portalcubed.content.command.FizzleCommand;
import io.github.fusionflux.portalcubed.content.command.PortalCommand;
import io.github.fusionflux.portalcubed.framework.command.CollectionSmuggler;
import io.github.fusionflux.portalcubed.framework.extension.RequiredArgumentBuilderExt;
import io.github.fusionflux.portalcubed.mixin.CommandContextAccessor;
import net.fabricmc.fabric.api.util.TriState;

import org.quiltmc.qsl.command.api.CommandRegistrationCallback;

import static net.minecraft.commands.Commands.literal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class PortalCubedCommands {
	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, ctx, env) -> dispatcher.register(
				literal(PortalCubed.ID)
						.then(CreateConstructCommand.build())
						.then(FizzleCommand.build())
						.then(PortalCommand.build())
		));
	}

	public static ArgumentBuilder<CommandSourceStack, ?> collection(Collection<? extends ArgumentBuilder<CommandSourceStack, ?>> collection) {
		return new CollectionSmuggler<>(collection);
	}

	public static <T> RequiredArgumentBuilder<CommandSourceStack, T> optionalArg(String name, ArgumentType<T> type) {
		RequiredArgumentBuilder<CommandSourceStack, T> arg = Commands.argument(name, type);
		((RequiredArgumentBuilderExt) arg).pc$setOptional(true);
		return arg;
	}

	public static <S, T> Optional<T> getOptional(CommandContext<S> ctx, String name, BiFunction<CommandContext<S>, String, T> getter) {
		return Optional.ofNullable(getOptional(ctx, name, getter, null));
	}

	public static <S, T> T getOptional(CommandContext<S> ctx, String name, BiFunction<CommandContext<S>, String, T> getter, T fallback) {
		Map<String, ParsedArgument<S, ?>> args = ((CommandContextAccessor<S>) ctx).getArguments();
		if (args.containsKey(name)) {
			return getter.apply(ctx, name);
		} else {
			return fallback;
		}
	}

	public static <S> TriState getOptionalBool(CommandContext<S> ctx, String name) {
		return TriState.of(getOptional(ctx, name, BoolArgumentType::getBool, null));
	}

	public static <S> boolean hasArgument(CommandContext<S> ctx, String name) {
		Map<String, ParsedArgument<S, ?>> args = ((CommandContextAccessor<S>) ctx).getArguments();
		return args.containsKey(name);
	}
}
