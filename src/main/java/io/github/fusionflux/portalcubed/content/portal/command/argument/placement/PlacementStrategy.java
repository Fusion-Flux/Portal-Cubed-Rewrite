package io.github.fusionflux.portalcubed.content.portal.command.argument.placement;

import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.getOptional;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.optionalArg;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.Locale;
import java.util.function.Consumer;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.fusionflux.portalcubed.content.PortalCubedCommands;
import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.PortalShot;
import io.github.fusionflux.portalcubed.content.portal.command.PortalCommand;
import io.github.fusionflux.portalcubed.content.portal.placement.PortalPlacement;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.NonePortalValidator;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.PortalValidator;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.StandardPortalValidator;
import io.github.fusionflux.portalcubed.framework.command.argument.DirectionArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.QuaternionArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public enum PlacementStrategy {
	PLACE_ON {
		@Override
		public ArgumentBuilder<CommandSourceStack, ?> build(Consumer<ArgumentBuilder<CommandSourceStack, ?>> innerModifier) {
			return literal(this.name).then(
					argument("position", BlockPosArgument.blockPos()).then(
							argument("facing", DirectionArgumentType.direction()).then(
									modify(optionalArg("rotation", FloatArgumentType.floatArg(0, 360)), innerModifier)
							)
					)
			);
		}

		@Override
		public Placement getPlacement(PortalId portal, CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
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

			PortalValidator validator = new StandardPortalValidator(PortalData.normalToFlatRotation(facing, rot));
			return new Placement(pos, rotation, validator);
		}
	},
	SHOT_FROM {
		@Override
		public ArgumentBuilder<CommandSourceStack, ?> build(Consumer<ArgumentBuilder<CommandSourceStack, ?>> innerModifier) {
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
		public Placement getPlacement(PortalId portal, CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
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

			return switch (PortalShot.perform(portal, level, start, normal, yaw)) {
				case PortalShot.Failed ignored -> throw PortalCommand.INVALID.create();
				case PortalShot.Missed ignored -> {
					int range = level.getGameRules().getInt(PortalCubedGameRules.PORTAL_SHOT_RANGE_LIMIT);
					throw PortalCommand.MISSED.create(range);
				}
				case PortalShot.Success success -> {
					PortalPlacement placement = success.placement;
					PortalValidator validator = new StandardPortalValidator(placement.rotationAngle());
					yield new Placement(placement.pos(), placement.rotation(), validator);
				}
			};
		}
	},
	PLACE_AT {
		@Override
		public ArgumentBuilder<CommandSourceStack, ?> build(Consumer<ArgumentBuilder<CommandSourceStack, ?>> innerModifier) {
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
		public Placement getPlacement(PortalId portal, CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
			Vec3 pos = Vec3Argument.getVec3(ctx, "position");
			Quaternionf rotation = PortalCommand.getRotation(ctx);
			return new Placement(pos, rotation, NonePortalValidator.INSTANCE);
		}
	};

	public final String name = this.name().toLowerCase(Locale.ROOT);

	public abstract ArgumentBuilder<CommandSourceStack, ?> build(Consumer<ArgumentBuilder<CommandSourceStack, ?>> innerModifier);

	public abstract Placement getPlacement(PortalId portal, CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;

	protected static <T extends ArgumentBuilder<CommandSourceStack, T>> T modify(T inner, Consumer<ArgumentBuilder<CommandSourceStack, ?>> consumer) {
		consumer.accept(inner);
		return inner;
	}
}
