package io.github.fusionflux.portalcubed.content.command;

import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.collection;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.getOptional;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.getOptionalBool;
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
import java.util.UUID;
import java.util.function.Consumer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import com.mojang.brigadier.context.CommandContext;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import io.github.fusionflux.portalcubed.content.PortalCubedCommands;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.content.portal.PortalShape;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.content.portal.projectile.PortalProjectile;
import io.github.fusionflux.portalcubed.framework.command.argument.ColorArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.DirectionArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalTypeArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalShapeArgumentType;

import io.github.fusionflux.portalcubed.framework.command.argument.QuaternionArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.TriStateArgumentType;
import net.fabricmc.fabric.api.util.TriState;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class PortalCommand {
	public static final String LANG_PREFIX = "commands.portalcubed.portal.";

	public static final String CREATE_FAILURE = "create.failure";
	public static final Component ID_ALL = lang(CREATE_FAILURE + ".id_all");
	public static final Component ID_TOO_LONG = lang(CREATE_FAILURE + ".id_too_long");

	public static final Component MODIFY_SUCCESS = lang("modify.success");
	public static final String MODIFY_FAILURE = "modify.failure";
	public static final String MODIFY_NONEXISTENT = MODIFY_FAILURE + ".nonexistent";
	public static final String INVALID_RENDERING = MODIFY_FAILURE + ".invalid_rendering";
	public static final String MODIFY_UNCHANGED = MODIFY_FAILURE + ".unchanged";

	public static final Component REMOVE_SINGLE = lang("remove.success");
	public static final Component REMOVE_MULTI = lang("remove.success.multiple");
	public static final Component REMOVE_ALL = lang("remove.success.all");

	public static final String REMOVE_FAIL = "remove.failure";
	public static final String REMOVE_FAIL_MULTI = REMOVE_FAIL + ".multiple";
	public static final String REMOVE_NONEXISTENT = REMOVE_FAIL + ".nonexistent";
	public static final String REMOVE_NONEXISTENT_MULTI = REMOVE_NONEXISTENT + ".multiple";
	public static final Component NO_PORTALS = lang(REMOVE_FAIL + ".no_portals");

	public static final SimpleCommandExceptionType MISSED = new SimpleCommandExceptionType(
			lang("create.failure.shot_from.miss")
	);

	public static LiteralArgumentBuilder<CommandSourceStack> build() {
		return literal("portal")
				.then(
						literal("create").then(
								argument("key", StringArgumentType.string()).then(
										argument("polarity", PortalTypeArgumentType.portalType()).then(collection(
												Arrays.stream(PlacementStrategy.values())
														.map(strategy -> strategy.build(inner -> inner.then(
																optionalArg("shape", PortalShapeArgumentType.shape()).then(
																		optionalArg("color", ColorArgumentType.color()).then(
																				optionalArg("render", BoolArgumentType.bool()).then(
																						optionalArg("validate", BoolArgumentType.bool())
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
								argument("key", StringArgumentType.string()).then(
										argument("polarity", PortalTypeArgumentType.portalType()).then(collection(
												Arrays.stream(PortalAttribute.values())
														.map(attribute -> literal(attribute.name).then(
																attribute.build(
																		ctx -> modify(ctx, attribute)
																))
														)
														.toList()
										))
								)
						)
				).then(
						literal("remove")
								.then(
										argument("key", StringArgumentType.string()).then(
												optionalArg("polarity", PortalTypeArgumentType.portalType())
														.executes(PortalCommand::remove)
										)
								).then(
										literal("all").executes(PortalCommand::removeAll)
								)
				);
	}

	private static int create(CommandContext<CommandSourceStack> ctx, PlacementStrategy strategy) throws CommandSyntaxException{
		Placement placement = strategy.getPlacement(ctx);
		String key = StringArgumentType.getString(ctx, "key");
		PortalType type = PortalTypeArgumentType.getPortalType(ctx, "polarity");
		PortalShape shape = getOptional(ctx, "shape", PortalShapeArgumentType::getShape, PortalShape.SQUARE);
		int color = getOptional(ctx, "color", ColorArgumentType::getColor, type.defaultColor);
		TriState render = getOptionalBool(ctx, "render");
		boolean validate = getOptional(ctx, "validate", BoolArgumentType::getBool, true);

		if ("all".equals(key)) {
			return fail(ctx, CREATE_FAILURE, ID_ALL);
		} else if (key.length() > 32) {
			return fail(ctx, CREATE_FAILURE, ID_TOO_LONG);
		}

		// TODO: custom portal types
//		boolean supportsRendering = true;
//		if (render == TriState.TRUE && !supportsRendering) {
//			return fail(ctx, CREATE_FAILURE, lang("create.failure.invalid_rendering", typeId));
//		}

		UUID id = PortalManager.generateId(key);
		ServerPortalManager manager = ctx.getSource().getLevel().portalManager();
		PortalPair pair = manager.getPair(id);
		if (pair != null && pair.get(type).isPresent()) {
			return fail(ctx, CREATE_FAILURE, lang("create.failure.already_exists", key, type));
		}

		PortalSettings settings = new PortalSettings(color, shape);
		PortalData data = new PortalData(placement.pos, placement.rotation, settings);
		manager.createPortal(id, type, data);
		ctx.getSource().sendSuccess(() -> lang("create.success"), true);
		return Command.SINGLE_SUCCESS;
	}

	private static int modify(CommandContext<CommandSourceStack> ctx, PortalAttribute attribute) throws CommandSyntaxException {
		String key = StringArgumentType.getString(ctx, "key");
		PortalType type = PortalTypeArgumentType.getPortalType(ctx, "polarity");

		ServerPortalManager manager = ctx.getSource().getLevel().portalManager();
		UUID id = PortalManager.generateId(key);

		PortalPair pair = manager.getPair(id);
		if (pair == null || pair.get(type).isEmpty()) {
			return fail(ctx, MODIFY_FAILURE, lang(MODIFY_NONEXISTENT, key, type));
		}
		PortalInstance portal = pair.getOrThrow(type);
		PortalData newData = attribute.modify(ctx, portal.data);
		if (newData == null)
			return 0;

		manager.setPair(id, pair.with(type, new PortalInstance(newData)));

		ctx.getSource().sendSuccess(() -> MODIFY_SUCCESS, true);
		return Command.SINGLE_SUCCESS;
	}

	private static int remove(CommandContext<CommandSourceStack> ctx) {
		String key = StringArgumentType.getString(ctx, "key");
		Optional<PortalType> maybeType = getOptional(ctx, "polarity", PortalTypeArgumentType::getPortalType);

		CommandSourceStack source = ctx.getSource();
		ServerPortalManager manager = source.getLevel().portalManager();
		UUID id = PortalManager.generateId(key);
		PortalPair pair = manager.getPair(id);

		if (maybeType.isEmpty()) {
			// remove both
			if (pair == null || pair.isEmpty()) {
				return fail(ctx, REMOVE_FAIL_MULTI, lang(REMOVE_NONEXISTENT_MULTI, key));
			}

			manager.setPair(id, null);
			source.sendSuccess(() -> REMOVE_MULTI, true);
		} else {
			PortalType type = maybeType.get();
			if (pair == null || pair.get(type).isEmpty()) {
				return fail(ctx, REMOVE_FAIL, lang(REMOVE_NONEXISTENT, key));
			}

			manager.setPair(id, pair.without(type));
			source.sendSuccess(() -> REMOVE_SINGLE, true);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int removeAll(CommandContext<CommandSourceStack> ctx) {
		ServerPortalManager manager = ctx.getSource().getLevel().portalManager();
		Set<UUID> ids = manager.getAllIds();
		if (ids.isEmpty()) {
			return fail(ctx, REMOVE_FAIL, NO_PORTALS);
		}

		// copy to avoid CME
		Set<UUID> copy = new HashSet<>(ids);
		copy.forEach(id -> manager.setPair(id, null));
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
		return Component.translatable(LANG_PREFIX + key, args);
	}

	private record Placement(Vec3 pos, Quaternionf rotation) {
		@Override
		public String toString() {
			return "x=" + this.pos.x + ", y=" + this.pos.y + ", z=" + this.pos.z + ", rot=" + this.rotation;
		}
	}

	private static Quaternionf getRotation(CommandContext<CommandSourceStack> ctx) {
		if (hasArgument(ctx, "facing")) {
			Direction facing = DirectionArgumentType.getDirection(ctx, "facing");
			return PortalProjectile.getPortalRotation(facing, 0);
		} else if (hasArgument(ctx, "rotation")) {
			Coordinates coords = RotationArgument.getRotation(ctx, "rotation");
			Vec2 rotations = coords.getRotation(ctx.getSource());
			return new Quaternionf()
					.rotateX(Mth.DEG_TO_RAD * rotations.x)
					.rotateY(Mth.DEG_TO_RAD * rotations.y);
		} else {
			return QuaternionArgumentType.getQuaternion(ctx, "quaternion");
		}
	}

	private enum PlacementStrategy {
		PLACE_ON {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(Consumer<ArgumentBuilder<CommandSourceStack, ?>> innerModifier) {
				return literal(this.name).then(
						argument("pos", BlockPosArgument.blockPos()).then(
								argument("facing", DirectionArgumentType.direction()).then(
										modify(optionalArg("yRot", FloatArgumentType.floatArg(0, 360)), innerModifier)
								)
						)
				);
			}

			@Override
			protected Placement getPlacement(CommandContext<CommandSourceStack> ctx) {
				BlockPos blockPos = BlockPosArgument.getBlockPos(ctx, "pos");
				Direction facing = DirectionArgumentType.getDirection(ctx, "facing");
				float yRot = getOptional(ctx, "yRot", FloatArgumentType::getFloat, 0f);

				Quaternionf rotation = PortalProjectile.getPortalRotation(facing, yRot);
				// shift the portal so the bottom half is centered on the surface
				Vector3f baseOffset = new Vector3f(0, 0.5f, 0);
				Vector3f offset = rotation.transform(baseOffset);

				Vec3 pos = Vec3.atCenterOf(blockPos)
						.add(facing.getStepX() / 2f, facing.getStepY() / 2f, facing.getStepZ() / 2f)
						.add(offset.x, offset.y, offset.z);

				return new Placement(pos, rotation);
			}
		},
		SHOT_FROM {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(Consumer<ArgumentBuilder<CommandSourceStack, ?>> innerModifier) {
				return literal(this.name).then(
						argument("pos", Vec3Argument.vec3())
								.then(
										modify(argument("facing", DirectionArgumentType.direction()), innerModifier)
								).then(
										modify(argument("rotation", RotationArgument.rotation()), innerModifier)
								)
				);
			}

			@Override
			protected Placement getPlacement(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
				Vec3 start = Vec3Argument.getVec3(ctx, "pos");
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
				Vec3 end = start.add(normal.scale(16));

				ClipContext clip = new ClipContext(
						start, end,
						ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
						CollisionContext.empty()
				);

				BlockHitResult hit = ctx.getSource().getLevel().clip(clip);
				if (hit.getType() == HitResult.Type.BLOCK) {
					Vec3 pos = hit.getLocation();
					Direction facing = hit.getDirection();
					Quaternionf rotation = PortalProjectile.getPortalRotation(facing, yaw + 180);
					return new Placement(pos, rotation);
				}

				throw MISSED.create();
			}
		},
		PLACE_AT {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(Consumer<ArgumentBuilder<CommandSourceStack, ?>> innerModifier) {
				return literal(this.name).then(
						argument("pos", Vec3Argument.vec3())
								.then(
										modify(argument("facing", DirectionArgumentType.direction()), innerModifier)
								).then(
										modify(argument("rotation", RotationArgument.rotation()), innerModifier)
								).then(
										modify(argument("quaternion", QuaternionArgumentType.quaternion()), innerModifier)
								)
				);
			}

			@Override
			protected Placement getPlacement(CommandContext<CommandSourceStack> ctx) {
				Vec3 pos = Vec3Argument.getVec3(ctx, "pos");
				Quaternionf rotation = getRotation(ctx);
				return new Placement(pos, rotation);
			}
		};

		protected final String name = this.name().toLowerCase(Locale.ROOT);

		protected abstract ArgumentBuilder<CommandSourceStack, ?> build(Consumer<ArgumentBuilder<CommandSourceStack, ?>> innerModifier);

		protected abstract Placement getPlacement(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;

		protected static <T extends ArgumentBuilder<CommandSourceStack, T>> T modify(T inner, Consumer<ArgumentBuilder<CommandSourceStack, ?>> consumer) {
			consumer.accept(inner);
			return inner;
		}
	}

	private enum PortalAttribute {
		PLACEMENT {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(Command<CommandSourceStack> command) {
				return collection(
						Arrays.stream(PlacementStrategy.values())
								.map(strategy -> strategy.build(inner -> inner.executes(command)))
								.toList()
				);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalData portal) throws CommandSyntaxException {
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
		POS {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(Command<CommandSourceStack> command) {
				return argument("pos", Vec3Argument.vec3())
						.executes(command);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalData portal) {
				Vec3 pos = Vec3Argument.getVec3(ctx, "pos");
				return portal.origin().equals(pos)
						? this.fail(ctx, pos)
						: portal.withOrigin(pos);
			}
		},
		ROTATION {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(Command<CommandSourceStack> command) {
				return collection(List.of(
						argument("facing", DirectionArgumentType.direction()).executes(command),
						argument("rotation", RotationArgument.rotation()).executes(command),
						argument("quaternion", QuaternionArgumentType.quaternion()).executes(command)
				));
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalData portal) {
				Quaternionf rotation = getRotation(ctx);
				return portal.rotation().equals(rotation)
						? this.fail(ctx, rotation)
						: portal.withRotation(rotation);
			}
		},
		SHAPE {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(Command<CommandSourceStack> command) {
				return argument("shape", PortalShapeArgumentType.shape())
						.executes(command);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalData portal) {
				PortalShape shape = PortalShapeArgumentType.getShape(ctx, "shape");
				return portal.settings().shape() == shape
						? this.fail(ctx, shape.name)
						: portal.withSettings(portal.settings().withShape(shape));
			}
		},
		COLOR {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(Command<CommandSourceStack> command) {
				return argument("color", ColorArgumentType.color())
						.executes(command);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalData portal) {
				int color = ColorArgumentType.getColor(ctx, "color");
				return portal.settings().color() == PortalSettings.fixAlpha(color)
						? this.fail(ctx, "#" + Integer.toHexString(color))
						: portal.withSettings(portal.settings().withColor(color));
			}
		},
		RENDER {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(Command<CommandSourceStack> command) {
				return argument("render", TriStateArgumentType.triState())
						.executes(command);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalData portal) {
				TriState render = TriStateArgumentType.getTriState(ctx, "render");
				// TODO: check if type supports rendering
				boolean shouldRender = render.orElse(true);
				return portal.settings().render() == shouldRender
						? this.fail(ctx, shouldRender)
						: portal.withSettings(portal.settings().withRender(shouldRender));
			}
		},
		VALIDATE {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(Command<CommandSourceStack> command) {
				return argument("validate", BoolArgumentType.bool())
						.executes(command);
			}

			@Override
			protected PortalData modify(CommandContext<CommandSourceStack> ctx, PortalData portal) {
				boolean validate = BoolArgumentType.getBool(ctx, "validate");
				return portal.settings().validate() == validate
						? this.fail(ctx, validate)
						: portal.withSettings(portal.settings().withValidate(validate));
			}
		};

		private final String name = this.name().toLowerCase(Locale.ROOT);

		protected abstract ArgumentBuilder<CommandSourceStack, ?> build(Command<CommandSourceStack> command);

		protected abstract PortalData modify(CommandContext<CommandSourceStack> ctx, PortalData portal) throws CommandSyntaxException;

		protected PortalData fail(CommandContext<CommandSourceStack> ctx, Object value) {
			ctx.getSource().sendFailure(lang(MODIFY_UNCHANGED, this.name, value));
			return null;
		}
	}
}
