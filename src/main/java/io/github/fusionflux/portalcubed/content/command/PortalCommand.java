package io.github.fusionflux.portalcubed.content.command;

import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.collection;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.getOptional;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.getOptionalBool;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.optionalArg;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

	public static final Component REMOVE_SINGLE = lang("remove.success");
	public static final Component REMOVE_MULTI = lang("remove.success.multiple");
	public static final Component REMOVE_ALL = lang("remove.success.all");

	public static final String REMOVE_FAIL = "remove.failure";
	public static final String REMOVE_FAIL_MULTI = REMOVE_FAIL + ".multiple";
	public static final String NONEXISTENT = REMOVE_FAIL + ".nonexistent";
	public static final String NONEXISTENT_MULTI = NONEXISTENT + ".multiple";
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
														.map(strategy -> strategy.build(
																optionalArg("shape", PortalShapeArgumentType.shape()).then(
																		optionalArg("color", ColorArgumentType.color()).then(
																				optionalArg("render", BoolArgumentType.bool()).then(
																						optionalArg("validate", BoolArgumentType.bool())
																								.executes(ctx -> create(ctx, strategy))
																				)
																		)
																)
														))
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
				return fail(ctx, REMOVE_FAIL_MULTI, lang(NONEXISTENT_MULTI, key));
			}

			manager.setPair(id, null);
			source.sendSuccess(() -> REMOVE_MULTI, true);
		} else {
			PortalType type = maybeType.get();
			if (pair == null || pair.get(type).isEmpty()) {
				return fail(ctx, REMOVE_FAIL, lang(NONEXISTENT, key));
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
	}

	private enum PlacementStrategy {
		PLACE_ON {
			@Override
			protected ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> next) {
				return literal("place_on").then(
						argument("pos", BlockPosArgument.blockPos()).then(
								argument("facing", DirectionArgumentType.direction()).then(
										optionalArg("yRot", FloatArgumentType.floatArg(0, 360))
												.then(next)
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
			protected ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> next) {
				return literal("shot_from").then(
						argument("pos", Vec3Argument.vec3())
								.then(
										argument("facing", DirectionArgumentType.direction())
												.then(next)
								).then(
										argument("rotation", RotationArgument.rotation())
												.then(next)
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
			protected ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> next) {
				return literal("place_at").then(
						argument("pos", Vec3Argument.vec3())
								.then(
										argument("facing", DirectionArgumentType.direction())
												.then(next)
								).then(
										argument("rotation", RotationArgument.rotation())
												.then(next)
								).then(
										argument("quaternion", QuaternionArgumentType.quaternion())
												.then(next)
								)
				);
			}

			@Override
			protected Placement getPlacement(CommandContext<CommandSourceStack> ctx) {
				Vec3 pos = Vec3Argument.getVec3(ctx, "pos");
				if (PortalCubedCommands.hasArgument(ctx, "facing")) {
					Direction facing = DirectionArgumentType.getDirection(ctx, "facing");
					Quaternionf rotation = PortalProjectile.getPortalRotation(facing, 0);
					return new Placement(pos, rotation);
				} else if (PortalCubedCommands.hasArgument(ctx, "rotation")) {
					Coordinates coords = RotationArgument.getRotation(ctx, "rotation");
					Vec2 rotations = coords.getRotation(ctx.getSource());
					Quaternionf rotation = new Quaternionf()
							.rotateX(Mth.DEG_TO_RAD * rotations.x)
							.rotateY(Mth.DEG_TO_RAD * rotations.y);
					return new Placement(pos, rotation);
				} else {
					Quaternionf rotation = QuaternionArgumentType.getQuaternion(ctx, "quaternion");
					return new Placement(pos, rotation);
				}
			}
		};

		protected abstract ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> next);

		protected abstract Placement getPlacement(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
	}
}
