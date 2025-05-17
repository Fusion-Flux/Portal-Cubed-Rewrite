package io.github.fusionflux.portalcubed.content.command;

import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.collection;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.flag;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.getFlag;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.getOptional;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.hasArgument;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.optionalArg;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import io.github.fusionflux.portalcubed.content.PortalCubedCommands;
import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.content.PortalCubedSuggestionProviders;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunShootContext;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.NonePortalValidator;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.PortalValidator;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.StandardPortalValidator;
import io.github.fusionflux.portalcubed.framework.command.argument.ColorArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.DirectionArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PolarityArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalKeyArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalValidatorArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.QuaternionArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.TriStateArgumentType;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class PortalCommand {
	public static final String LANG_PREFIX = "commands.portalcubed.portal.";

	public static final String CREATE_FAILURE = "create.failure";
	public static final Component ID_ALL = lang(CREATE_FAILURE + ".id_all");
	public static final Component ID_TOO_LONG = lang(CREATE_FAILURE + ".id_too_long");

	public static final Component MODIFY_SUCCESS = lang("modify.success");
	public static final String MODIFY_FAILURE = "modify.failure";
	public static final String MODIFY_NONEXISTENT = MODIFY_FAILURE + ".nonexistent";
	public static final String MODIFY_UNCHANGED = MODIFY_FAILURE + ".unchanged";

	public static final Component REMOVE_SINGLE = lang("remove.success");
	public static final Component REMOVE_MULTI = lang("remove.success.multiple");
	public static final Component REMOVE_ALL = lang("remove.success.all");

	public static final String REMOVE_FAIL = "remove.failure";
	public static final String REMOVE_FAIL_MULTI = REMOVE_FAIL + ".multiple";
	public static final String REMOVE_NONEXISTENT = REMOVE_FAIL + ".nonexistent";
	public static final String REMOVE_NONEXISTENT_MULTI = REMOVE_NONEXISTENT + ".multiple";
	public static final Component NO_PORTALS = lang(REMOVE_FAIL + ".no_portals");

	public static final DynamicCommandExceptionType MISSED = new DynamicCommandExceptionType(
			range -> lang("create.failure.shot_from.miss", range)
	);

	public static Holder.Reference<PortalType> getType(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
		return ResourceArgument.getResource(ctx, name, PortalCubedRegistries.PORTAL_TYPE);
	}

	public static LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildCtx) {
		return literal("portal")
				.requires(source -> source.hasPermission(2))
				.then(
						literal("create").then(
								argument("key", PortalKeyArgumentType.portalKey())
										.suggests(PortalCubedSuggestionProviders.PORTAL_CREATION_KEYS)
										.then(
												argument("polarity", PolarityArgumentType.polarity()).then(collection(
														Arrays.stream(PlacementStrategy.values())
																.map(strategy -> strategy.build(inner -> inner.then(
																		optionalArg("type", ResourceArgument.resource(buildCtx, PortalCubedRegistries.PORTAL_TYPE)).then(
																				optionalArg("color", ColorArgumentType.color()).then(
																						flag("no_rendering").then(
																								optionalArg("validator", PortalValidatorArgumentType.portalValidator())
																										.executes(ctx -> create(ctx, strategy))
																						)
																				)
																		)
																)))
																.toList()
												))
										)
						)
				).then(
						literal("modify").then(
								argument("key", PortalKeyArgumentType.portalKey()).then(
										argument("polarity", PolarityArgumentType.polarity()).then(collection(
												Arrays.stream(PortalAttribute.values())
														.map(attribute -> literal(attribute.name).then(
																attribute.build(buildCtx,
																		ctx -> modify(ctx, attribute)))
														)
														.toList()
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

		Placement placement = strategy.getPlacement(ctx);
		String key = PortalKeyArgumentType.getKey(ctx, "key");
		Polarity polarity = PolarityArgumentType.getPolarity(ctx, "polarity");
		Holder<PortalType> type = getOptional(ctx, "type", PortalCommand::getType, source.registryAccess().get(PortalType.ROUND).orElseThrow());
		int color = getOptional(ctx, "color", ColorArgumentType::getColor, type.value().defaultColorOf(polarity));
		boolean noRender = getFlag(ctx, "no_rendering");
		PortalValidator validator = getOptional(
				ctx, "validator", PortalValidatorArgumentType::getPortalValidator, strategy::getFallbackValidator
		).build(ctx);

		if ("all".equals(key)) {
			return fail(ctx, CREATE_FAILURE, ID_ALL);
		} else if (key.length() > 32) {
			return fail(ctx, CREATE_FAILURE, ID_TOO_LONG);
		}

		ServerLevel level = source.getLevel();
		ServerPortalManager manager = level.portalManager();
		PortalPair pair = manager.getPair(key);
		if (pair != null && pair.get(polarity).isPresent()) {
			return fail(ctx, CREATE_FAILURE, lang("create.failure.already_exists", key, polarity));
		}

		PortalData data = new PortalData(level.getGameTime(), type, validator, placement.pos, placement.rotation, ARGB.opaque(color), !noRender);
		manager.createPortal(key, polarity, data);
		source.sendSuccess(() -> lang("create.success"), true);
		return Command.SINGLE_SUCCESS;
	}

	private static int modify(CommandContext<CommandSourceStack> ctx, PortalAttribute attribute) throws CommandSyntaxException {
		String key = PortalKeyArgumentType.getKey(ctx, "key");
		Polarity polarity = PolarityArgumentType.getPolarity(ctx, "polarity");

		ServerPortalManager manager = ctx.getSource().getLevel().portalManager();
		PortalPair pair = manager.getPair(key);

		if (pair == null || pair.get(polarity).isEmpty()) {
			return fail(ctx, MODIFY_FAILURE, lang(MODIFY_NONEXISTENT, key, polarity));
		}
		PortalInstance portal = pair.getOrThrow(polarity);
		PortalData newData = attribute.modify(ctx, polarity, portal.data);
		if (newData == null)
			return 0;

		manager.setPair(key, pair.with(polarity, new PortalInstance(newData)));

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
				return fail(ctx, REMOVE_FAIL_MULTI, lang(REMOVE_NONEXISTENT_MULTI, key));
			}

			manager.setPair(key, null);
			source.sendSuccess(() -> REMOVE_MULTI, true);
		} else {
			Polarity polarity = maybePolarity.get();
			if (pair == null || pair.get(polarity).isEmpty()) {
				return fail(ctx, REMOVE_FAIL, lang(REMOVE_NONEXISTENT, key));
			}

			manager.setPair(key, pair.without(polarity));
			source.sendSuccess(() -> REMOVE_SINGLE, true);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int removeAll(CommandContext<CommandSourceStack> ctx) {
		ServerPortalManager manager = ctx.getSource().getLevel().portalManager();
		Set<String> keys = manager.getAllKeys();
		if (keys.isEmpty()) {
			return fail(ctx, REMOVE_FAIL, NO_PORTALS);
		}

		// copy to avoid CME
		Set<String> copy = new HashSet<>(keys);
		copy.forEach(key -> manager.setPair(key, null));
		ctx.getSource().sendSuccess(() -> REMOVE_ALL, true);
		return Command.SINGLE_SUCCESS;
	}

	private static int fail(CommandContext<CommandSourceStack> ctx, String key, Component argument) {
		ctx.getSource().sendFailure(lang(key, argument));
		return 0;
	}

	private static Component lang(String key) {
		return Component.translatable(LANG_PREFIX + key);
	}

	private static Component lang(String key, Object... args) {
		return Component.translatableEscape(LANG_PREFIX + key, args);
	}

	private record Placement(Vec3 pos, Quaternionf rotation) {
		@Override
		public String toString() {
			return "x=" + this.pos.x + ", y=" + this.pos.y + ", z=" + this.pos.z + ", rot=" + this.rotation;
		}
	}

	private static Quaternionf getRotation(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		if (hasArgument(ctx, "facing")) {
			Direction facing = DirectionArgumentType.getDirection(ctx, "facing");
			float rot = getOptional(ctx, "rotation", FloatArgumentType::getFloat, 0f);
			return PortalData.normalToRotation(facing, rot);
		} else if (hasArgument(ctx, "rotation")) {
			Coordinates coords = RotationArgument.getRotation(ctx, "rotation");
			Vec2 rotations = coords.getRotation(ctx.getSource());
			// x is pitch, y is yaw
			return new Quaternionf()
					// a plain quaternion is UP, rotate to face south
					// spin it around first though, so it's not upside down
					.rotateY(Mth.DEG_TO_RAD * 180)
					.rotateX(Mth.DEG_TO_RAD * -90)
					// apply actual rotation
					.rotateZ(Mth.DEG_TO_RAD * rotations.y)
					.rotateX(Mth.DEG_TO_RAD * -rotations.x);
		} else {
			return QuaternionArgumentType.getQuaternion(ctx, "quaternion");
		}
	}

	private enum PlacementStrategy {
		PLACE_ON {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(Consumer<ArgumentBuilder<CommandSourceStack, ?>> innerModifier) {
				return literal(this.name).then(
						argument("position", BlockPosArgument.blockPos()).then(
								argument("facing", DirectionArgumentType.direction()).then(
										modify(optionalArg("rotation", FloatArgumentType.floatArg(0, 360)), innerModifier)
								)
						)
				);
			}

			@Override
			protected Placement getPlacement(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
				BlockPos blockPos = BlockPosArgument.getBlockPos(ctx, "position");
				Direction facing = DirectionArgumentType.getDirection(ctx, "facing");
				float rot = getOptional(ctx, "rotation", FloatArgumentType::getFloat, 0f);

				Quaternionf rotation = PortalData.normalToRotation(facing, 0);
				rotation.rotateZ(Mth.DEG_TO_RAD * rot);
				// shift the portal so the bottom half is centered on the surface
				Vector3f baseOffset = new Vector3f(0, 0.5f, 0);
				Vector3f offset = rotation.transform(baseOffset);

				Vec3 pos = Vec3.atCenterOf(blockPos)
						.add(facing.getStepX() / 2f, facing.getStepY() / 2f, facing.getStepZ() / 2f)
						.add(offset.x, offset.y, offset.z);

				return new Placement(pos, rotation);
			}

			@Override
			protected PortalValidator getFallbackValidator(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
				BlockPos pos = BlockPosArgument.getBlockPos(ctx, "position");
				Direction facing = DirectionArgumentType.getDirection(ctx, "facing");
				float rot = getOptional(ctx, "rotation", FloatArgumentType::getFloat, 0f);
				return new StandardPortalValidator(pos, facing, rot);
			}
		},
		SHOT_FROM {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(Consumer<ArgumentBuilder<CommandSourceStack, ?>> innerModifier) {
				return literal(this.name).then(
						argument("position", Vec3Argument.vec3())
								.then(
										modify(argument("facing", DirectionArgumentType.direction()), innerModifier)
								).then(
										modify(argument("rotation", RotationArgument.rotation()), innerModifier)
								)
				);
			}

			@Override
			protected Placement getPlacement(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
				Vec3 start = Vec3Argument.getVec3(ctx, "position");
				float pitch, yaw;
				if (PortalCubedCommands.hasArgument(ctx, "facing")) {
					Direction facing = DirectionArgumentType.getDirection(ctx, "facing");
					pitch = switch (facing) {
						case UP -> -90;
						case DOWN -> 90;
						default -> 0;
					};
					yaw = facing.toYRot();
				} else {
					Coordinates coords = RotationArgument.getRotation(ctx, "rotation");
					Vec2 rotations = coords.getRotation(ctx.getSource());
					pitch = rotations.x;
					yaw = rotations.y;
				}

				Vec3 normal = Vec3.directionFromRotation(pitch, yaw).normalize();
				ServerLevel level = ctx.getSource().getLevel();
				BlockHitResult hit = PortalGunShootContext.clip(level, start, normal);

				if (hit.getType() == HitResult.Type.BLOCK) {
					Vec3 pos = hit.getLocation();
					Direction facing = hit.getDirection();
					Quaternionf rotation = PortalData.normalToRotation(facing, yaw + 180);
					return new Placement(pos, rotation);
				}

				int range = level.getGameRules().getInt(PortalCubedGameRules.PORTAL_SHOT_RANGE_LIMIT);
				throw MISSED.create(range);
			}

			@Override
			protected PortalValidator getFallbackValidator(CommandContext<CommandSourceStack> ctx) {
				// FIXME: this is garbage
				Vec3 start = Vec3Argument.getVec3(ctx, "position");
				float pitch, yaw;
				if (PortalCubedCommands.hasArgument(ctx, "facing")) {
					Direction facing = DirectionArgumentType.getDirection(ctx, "facing");
					pitch = switch (facing) {
						case UP -> -90;
						case DOWN -> 90;
						default -> 0;
					};
					yaw = facing.toYRot();
				} else {
					Coordinates coords = RotationArgument.getRotation(ctx, "rotation");
					Vec2 rotations = coords.getRotation(ctx.getSource());
					pitch = rotations.x;
					yaw = rotations.y;
				}

				Vec3 normal = Vec3.directionFromRotation(pitch, yaw).normalize();
				ServerLevel level = ctx.getSource().getLevel();
				BlockHitResult hit = PortalGunShootContext.clip(level, start, normal);

				return new StandardPortalValidator(hit.getBlockPos(), hit.getDirection(), yaw + 180);
			}
		},
		PLACE_AT {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(Consumer<ArgumentBuilder<CommandSourceStack, ?>> innerModifier) {
				return literal(this.name).then(
						argument("position", Vec3Argument.vec3())
								.then(
										argument("facing", DirectionArgumentType.direction()).then(
												modify(optionalArg("rotation", FloatArgumentType.floatArg(0, 360)), innerModifier)
										)
								).then(
										modify(argument("rotation", RotationArgument.rotation()), innerModifier)
								).then(
										modify(argument("quaternion", QuaternionArgumentType.quaternion()), innerModifier)
								)
				);
			}

			@Override
			protected Placement getPlacement(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
				Vec3 pos = Vec3Argument.getVec3(ctx, "position");
				Quaternionf rotation = getRotation(ctx);
				return new Placement(pos, rotation);
			}

			@Override
			protected PortalValidator getFallbackValidator(CommandContext<CommandSourceStack> ctx) {
				return NonePortalValidator.INSTANCE;
			}
		};

		protected final String name = this.name().toLowerCase(Locale.ROOT);

		protected abstract ArgumentBuilder<CommandSourceStack, ?> build(Consumer<ArgumentBuilder<CommandSourceStack, ?>> innerModifier);

		protected abstract Placement getPlacement(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;

		protected abstract PortalValidator getFallbackValidator(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;

		protected static <T extends ArgumentBuilder<CommandSourceStack, T>> T modify(T inner, Consumer<ArgumentBuilder<CommandSourceStack, ?>> consumer) {
			consumer.accept(inner);
			return inner;
		}
	}

	private enum PortalAttribute {
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
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, Polarity polarity, PortalData portal) throws CommandSyntaxException {
				PlacementStrategy strategy = findStrategy(ctx);
				Placement placement = strategy.getPlacement(ctx);
				return portal.origin().equals(placement.pos) && portal.rotation().equals(placement.rotation)
						? this.fail(ctx, placement)
						: portal.withOrigin(placement.pos).withRotation(placement.rotation);
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
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, Polarity polarity, PortalData portal) {
				Vec3 pos = Vec3Argument.getVec3(ctx, "position");
				return portal.origin().equals(pos)
						? this.fail(ctx, pos)
						: portal.withOrigin(pos);
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
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, Polarity polarity, PortalData portal) throws CommandSyntaxException {
				Quaternionf rotation = getRotation(ctx);
				return portal.rotation().equals(rotation)
						? this.fail(ctx, rotation)
						: portal.withRotation(rotation);
			}
		},
		TYPE {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext ctx, Command<CommandSourceStack> command) {
				return argument("type", ResourceArgument.resource(ctx, PortalCubedRegistries.PORTAL_TYPE))
						.executes(command);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, Polarity polarity, PortalData portal) throws CommandSyntaxException {
				Holder<PortalType> type = PortalCommand.getType(ctx, "type");
				return portal.type() == type
						? this.fail(ctx, type.value().name())
						: portal.withType(type);
			}
		},
		COLOR {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext ctx, Command<CommandSourceStack> command) {
				return collection(List.of(
						argument("color", ColorArgumentType.color()).executes(command),
						literal("default").executes(command)
				));
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, Polarity polarity, PortalData portal) throws CommandSyntaxException {
				PortalType type = portal.type().value();
				int color = getOptional(ctx, "color", ColorArgumentType::getColor, type.defaultColorOf(polarity));
				return portal.color() == ARGB.opaque(color)
						? this.fail(ctx, "#" + Integer.toHexString(color))
						: portal.withColor(color);
			}
		},
		RENDER {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext ctx, Command<CommandSourceStack> command) {
				return argument("render", TriStateArgumentType.triState())
						.executes(command);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, Polarity polarity, PortalData portal) {
				TriState render = TriStateArgumentType.getTriState(ctx, "render");
				boolean shouldRender = render.orElse(true);
				return portal.render() == shouldRender
						? this.fail(ctx, shouldRender)
						: portal.withRender(shouldRender);
			}
		},
		VALIDATOR {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext ctx, Command<CommandSourceStack> command) {
				return argument("validator", PortalValidatorArgumentType.portalValidator())
						.executes(command);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, Polarity polarity, PortalData portal) throws CommandSyntaxException {
				PortalValidator validator = PortalValidatorArgumentType.getPortalValidator(ctx, "validator").build(ctx);
				return portal.validator().equals(validator)
						? this.fail(ctx, validator)
						: portal.withValidator(validator);
			}
		};

		private final String name = this.name().toLowerCase(Locale.ROOT);

		protected abstract ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext ctx, Command<CommandSourceStack> command);

		protected abstract PortalData modify(CommandContext<CommandSourceStack> ctx, Polarity polarity, PortalData portal) throws CommandSyntaxException;

		protected PortalData fail(CommandContext<CommandSourceStack> ctx, Object value) {
			ctx.getSource().sendFailure(lang(MODIFY_UNCHANGED, this.name, value));
			return null;
		}
	}
}
