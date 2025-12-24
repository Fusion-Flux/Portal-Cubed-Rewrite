package io.github.fusionflux.portalcubed.content.portal.command;

import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.collection;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.getOptional;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.hasArgument;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.optionalArg;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.joml.Quaternionf;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.content.PortalCubedSuggestionProviders;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.command.argument.placement.Placement;
import io.github.fusionflux.portalcubed.content.portal.command.argument.placement.PlacementStrategy;
import io.github.fusionflux.portalcubed.content.portal.command.argument.portal.PortalArgument;
import io.github.fusionflux.portalcubed.content.portal.command.argument.portal.PortalInput;
import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import io.github.fusionflux.portalcubed.content.portal.graphics.color.ConstantPortalColor;
import io.github.fusionflux.portalcubed.content.portal.graphics.color.PortalColor;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.PortalValidator;
import io.github.fusionflux.portalcubed.framework.command.argument.DirectionArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PolarityArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalColorArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalKeyArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalValidatorArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.QuaternionArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.TriStateArgumentType;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class PortalCommand {
	public static final String LANG_PREFIX = "commands.portalcubed.portal.";

	// create subcommand
	public static final Component CREATE_SUCCESS = lang("create.success");
	public static final DynamicCommandExceptionType CREATE_FAIL_ALREADY_EXISTS = dynamicException("create.failure.already_exists");

	// modify subcommand
	public static final Component MODIFY_SUCCESS = lang("modify.success");
	public static final DynamicCommandExceptionType MODIFY_FAIL_NONEXISTENT = dynamicException("modify.failure.nonexistent");
	public static final DynamicCommandExceptionType MODIFY_FAIL_INVALID_RENDERING = dynamicException("modify.failure.invalid_rendering");
	public static final SimpleCommandExceptionType MODIFY_FAIL_UNCHANGED = exception("modify.failure.unchanged");

	// remove subcommand
	public static final Component REMOVE_SINGLE = lang("remove.success.single");
	public static final Int2ObjectFunction<Component> REMOVE_MULTI = i -> lang("remove.success.multiple", i);
	public static final DynamicCommandExceptionType REMOVE_FAIL_NONEXISTENT = dynamicException("remove.failure.nonexistent");
	public static final DynamicCommandExceptionType REMOVE_FAIL_NONEXISTENT_PAIR = dynamicException("remove.failure.nonexistent.pair");
	public static final SimpleCommandExceptionType REMOVE_FAIL_NO_PORTALS = exception("remove.failure.no_portals");

	// PlacementStrategy exceptions
	public static final SimpleCommandExceptionType PLACE_ON_INVALID = exception("failure.place_on.invalid");
	public static final DynamicCommandExceptionType SHOT_FROM_MISSED = dynamicException("failure.shot_from.miss");
	public static final SimpleCommandExceptionType SHOT_FROM_INVALID = exception("failure.shot_from.invalid");
	public static final SimpleCommandExceptionType PLACEMENT_INVALID = exception("failure.placement.invalid");

	public static Holder.Reference<PortalType> getType(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
		return ResourceArgument.getResource(ctx, name, PortalCubedRegistries.PORTAL_TYPE);
	}

	public static LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context) {
		return literal("portal")
				.requires(source -> source.hasPermission(2))
				.then(
						literal("create").then(
								argument("key", PortalKeyArgumentType.portalKey())
										.suggests(PortalCubedSuggestionProviders.PORTAL_CREATION_KEYS)
										.then(argument("polarity", PolarityArgumentType.polarity()).then(
														argument("portal", PortalArgument.portal(context)).then(collection(
																Arrays.stream(PlacementStrategy.values()).map(
																		strategy -> strategy.build(inner -> inner.executes(ctx -> create(ctx, strategy)))
																).toList()
														))
												)
										)
						)
				).then(
						literal("modify").then(
								argument("key", PortalKeyArgumentType.portalKey()).then(
										argument("polarity", PolarityArgumentType.polarity()).then(collection(
												Arrays.stream(PortalModifier.values()).map(
														modifier -> literal(modifier.name).then(
																modifier.build(context, ctx -> modify(ctx, modifier))
														)
												).toList()
										))
								)
						)
				).then(
						literal("remove")
								.then(
										argument("key", PortalKeyArgumentType.portalKey()).then(
												optionalArg("polarity", PolarityArgumentType.polarity())
														.executes(PortalCommand::remove)
										)
								).then(
										literal("all").executes(PortalCommand::removeAll)
								)
				);
	}

	private static int create(CommandContext<CommandSourceStack> ctx, PlacementStrategy strategy) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		ServerLevel level = source.getLevel();

		String key = PortalKeyArgumentType.getKey(ctx, "key");
		Polarity polarity = PolarityArgumentType.getPolarity(ctx, "polarity");
		PortalInput input = PortalArgument.getPortal(ctx, "portal");

		PortalId id = new PortalId(key, polarity);
		Placement placement = strategy.getPlacement(id, ctx);

		ServerPortalManager manager = level.portalManager();
		if (manager.getPortal(id) != null) {
			throw CREATE_FAIL_ALREADY_EXISTS.create(id.component());
		}

		PortalColor defaultColor = new ConstantPortalColor(input.type().value().defaultColorOf(polarity));

		PortalData data = input.attributes().modify(new PortalData(
				level.getGameTime(), input.type(), placement.validator(),
				placement.pos(), placement.rotation(), defaultColor, true, true
		));

		checkValid(ctx, id, data);
		manager.createPortal(id, data);
		source.sendSuccess(() -> CREATE_SUCCESS, true);
		return Command.SINGLE_SUCCESS;
	}

	private static int modify(CommandContext<CommandSourceStack> ctx, PortalModifier modifier) throws CommandSyntaxException {
		String key = PortalKeyArgumentType.getKey(ctx, "key");
		Polarity polarity = PolarityArgumentType.getPolarity(ctx, "polarity");
		PortalId id = new PortalId(key, polarity);

		ServerLevel level = ctx.getSource().getLevel();
		ServerPortalManager manager = level.portalManager();
		PortalReference portal = manager.getPortal(id);

		if (portal == null) {
			throw MODIFY_FAIL_NONEXISTENT.create(id.component());
		}

		PortalData newData = modifier.modify(ctx, id, portal.get().data);
		if (portal.get().data.equals(newData)) {
			throw MODIFY_FAIL_UNCHANGED.create();
		}

		checkValid(ctx, portal.id, newData);
		manager.setPortal(id, newData);
		ctx.getSource().sendSuccess(() -> MODIFY_SUCCESS, true);
		return Command.SINGLE_SUCCESS;
	}

	private static int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		String key = PortalKeyArgumentType.getKey(ctx, "key");
		Optional<Polarity> maybePolarity = getOptional(ctx, "polarity", PolarityArgumentType::getPolarity);

		CommandSourceStack source = ctx.getSource();
		ServerPortalManager manager = source.getLevel().portalManager();
		PortalPair pair = manager.getPair(key);

		if (maybePolarity.isEmpty()) {
			// remove both
			if (pair == null || pair.isEmpty()) {
				throw REMOVE_FAIL_NONEXISTENT_PAIR.create(key);
			}

			manager.setPair(key, null);
			int removed = pair.size();
			source.sendSuccess(() -> REMOVE_MULTI.apply(removed), true);
			return removed;
		} else {
			Polarity polarity = maybePolarity.get();
			if (pair == null || pair.get(polarity).isEmpty()) {
				throw REMOVE_FAIL_NONEXISTENT.create(new PortalId(key, polarity).component());
			}

			manager.setPair(key, pair.without(polarity));
			source.sendSuccess(() -> REMOVE_SINGLE, true);
			return Command.SINGLE_SUCCESS;
		}
	}

	private static int removeAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPortalManager manager = ctx.getSource().getLevel().portalManager();
		Set<String> keys = manager.pairs().keySet();
		if (keys.isEmpty()) {
			throw REMOVE_FAIL_NO_PORTALS.create();
		}

		int portals = manager.portals().size();
		// copy to avoid a CME, since the backing collection will be modified with each call
		Set.copyOf(keys).forEach(key -> manager.setPair(key, null));
		ctx.getSource().sendSuccess(() -> REMOVE_MULTI.apply(portals), true);
		return portals;
	}

	private static Component lang(String key) {
		return Component.translatable(LANG_PREFIX + key);
	}

	private static Component lang(String key, Object... args) {
		return Component.translatableEscape(LANG_PREFIX + key, args);
	}

	private static SimpleCommandExceptionType exception(String key) {
		return new SimpleCommandExceptionType(lang(key));
	}

	private static DynamicCommandExceptionType dynamicException(String key) {
		return new DynamicCommandExceptionType(arg -> lang(key, arg));
	}

	private static void checkValid(CommandContext<CommandSourceStack> context, PortalId portal, PortalData data) throws CommandSyntaxException {
		ServerLevel level = context.getSource().getLevel();
		if (!data.validator().isValid(level, portal, new Portal(data))) {
			throw PLACEMENT_INVALID.create();
		}
	}

	public static Quaternionf getRotation(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		if (hasArgument(ctx, "facing")) {
			Direction facing = DirectionArgumentType.getDirection(ctx, "facing");
			float rot = getOptional(ctx, "rotation", FloatArgumentType::getFloat, 0f);
			return PortalData.normalToRotation(facing, rot);
		} else if (hasArgument(ctx, "rotation")) {
			Coordinates coords = RotationArgument.getRotation(ctx, "rotation");
			Vec2 rotations = coords.getRotation(ctx.getSource());
			// math based on Camera
			return new Quaternionf().rotationYXZ(
					Mth.PI - (rotations.y * Mth.DEG_TO_RAD),
					(-rotations.x - 90) * Mth.DEG_TO_RAD,
					0
			);
		} else {
			return QuaternionArgumentType.getQuaternion(ctx, "quaternion");
		}
	}

	private enum PortalModifier {
		PLACEMENT {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext ctx, Command<CommandSourceStack> command) {
				return collection(
						Arrays.stream(PlacementStrategy.values())
								.map(strategy -> strategy.build(inner -> inner.executes(command)))
								.toList()
				);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalId id, PortalData portal) throws CommandSyntaxException {
				PlacementStrategy strategy = findStrategy(ctx);
				Placement placement = strategy.getPlacement(id, ctx);
				return portal.withOrigin(placement.pos())
						.withRotation(placement.rotation())
						.withValidator(placement.validator());
			}

			private static PlacementStrategy findStrategy(CommandContext<CommandSourceStack> ctx) {
				for (PlacementStrategy strategy : PlacementStrategy.values()) {
					if (ctx.getInput().contains(strategy.name)) {
						return strategy;
					}
				}
				throw new IllegalStateException("Could not find PlacementStrategy");
			}
		},
		POSITION {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext ctx, Command<CommandSourceStack> command) {
				return argument("position", Vec3Argument.vec3())
						.executes(command);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalId id, PortalData portal) {
				Vec3 pos = Vec3Argument.getVec3(ctx, "position");
				return portal.withOrigin(pos);
			}
		},
		ROTATION {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext ctx, Command<CommandSourceStack> command) {
				return collection(List.of(
						argument("facing", DirectionArgumentType.direction()).then(
								optionalArg("rotation", FloatArgumentType.floatArg(0, 360))
										.executes(command)
						),
						argument("rotation", RotationArgument.rotation()).executes(command),
						argument("quaternion", QuaternionArgumentType.quaternion()).executes(command)
				));
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalId id, PortalData portal) throws CommandSyntaxException {
				Quaternionf rotation = getRotation(ctx);
				return portal.withRotation(rotation);
			}
		},
		TYPE {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext ctx, Command<CommandSourceStack> command) {
				return argument("type", ResourceArgument.resource(ctx, PortalCubedRegistries.PORTAL_TYPE))
						.executes(command);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalId id, PortalData portal) throws CommandSyntaxException {
				Holder<PortalType> type = PortalCommand.getType(ctx, "type");
				return portal.withType(type);
			}
		},
		COLOR {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext ctx, Command<CommandSourceStack> command) {
				return collection(List.of(
						argument("color", PortalColorArgumentType.portalColor()).executes(command),
						literal("default").executes(command)
				));
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalId id, PortalData portal) throws CommandSyntaxException {
				PortalType type = portal.type().value();
				PortalColor color = getOptional(ctx, "color", PortalColorArgumentType::getPortalColor).orElseGet(
						() -> new ConstantPortalColor(type.defaultColorOf(id.polarity()))
				);
				return portal.withColor(color);
			}
		},
		RENDER {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext ctx, Command<CommandSourceStack> command) {
				return argument("render", TriStateArgumentType.triState())
						.executes(command);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalId id, PortalData portal) throws CommandSyntaxException {
				TriState render = TriStateArgumentType.getTriState(ctx, "render");
				if (render == TriState.TRUE && !portal.type().value().supportsRendering()) {
					String typeName = portal.type().unwrapKey()
							.map(ResourceKey::location)
							.map(ResourceLocation::toString)
							.orElse("<unregistered>");

					throw MODIFY_FAIL_INVALID_RENDERING.create(typeName);
				}

				boolean shouldRender = render.orElse(true);
				return portal.withRender(shouldRender);
			}
		},
		TRACER {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext ctx, Command<CommandSourceStack> command) {
				return argument("tracer", BoolArgumentType.bool())
						.executes(command);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalId id, PortalData portal) {
				boolean tracer = BoolArgumentType.getBool(ctx, "tracer");
				return portal.withTracer(tracer);
			}
		},
		VALIDATOR {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext ctx, Command<CommandSourceStack> command) {
				return argument("validator", PortalValidatorArgumentType.portalValidator())
						.executes(command);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalId id, PortalData portal) throws CommandSyntaxException {
				PortalValidator validator = PortalValidatorArgumentType.getPortalValidator(ctx, "validator").build(ctx);
				return portal.withValidator(validator);
			}
		};

		private final String name = this.name().toLowerCase(Locale.ROOT);

		protected abstract ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext ctx, Command<CommandSourceStack> command);

		protected abstract PortalData modify(CommandContext<CommandSourceStack> ctx, PortalId id, PortalData portal) throws CommandSyntaxException;
	}
}
