package io.github.fusionflux.portalcubed.content.portal.command.argument.placement;

import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.getOptional;
import static io.github.fusionflux.portalcubed.content.PortalCubedCommands.optionalArg;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.Locale;
import java.util.function.Consumer;

import org.joml.Quaternionf;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.fusionflux.portalcubed.content.PortalCubedCommands;
import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
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
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
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

			PortalShot.Source source = PortalShot.Source.forPlacingOn(blockPos, facing, rot);
			ServerLevel level = ctx.getSource().getLevel();

			if (source.shoot(portal, level) instanceof PortalShot.Success success) {
				return new Placement(success.placement.pos(), success.placement.rotation(), success.createValidator());
			}

			throw PortalCommand.PLACE_ON_INVALID.create();
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
			).then(
					modify(argument("entity", EntityArgument.entity()), innerModifier)
			);
		}

		@Override
		public Placement getPlacement(PortalId portal, CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
			Source source = Source.get(ctx);

			Vec3 normal = Vec3.directionFromRotation(source.pitch, source.yaw).normalize();
			ServerLevel level = ctx.getSource().getLevel();

			return switch (PortalShot.perform(portal, level, source.pos, normal, source.yaw)) {
				case PortalShot.Failed ignored -> throw PortalCommand.SHOT_FROM_INVALID.create();
				case PortalShot.Missed ignored -> {
					int range = level.getGameRules().getInt(PortalCubedGameRules.PORTAL_SHOT_RANGE_LIMIT);
					throw PortalCommand.SHOT_FROM_MISSED.create(range);
				}
				case PortalShot.Success success -> {
					PortalPlacement placement = success.placement;
					PortalValidator validator = new StandardPortalValidator(placement.rotationAngle());
					yield new Placement(placement.pos(), placement.rotation(), validator);
				}
			};
		}

		private record Source(Vec3 pos, float pitch, float yaw) {
			private static Source get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
				if (PortalCubedCommands.hasArgument(ctx, "entity")) {
					Entity entity = EntityArgument.getEntity(ctx, "entity");
					return new Source(entity.getEyePosition(), entity.getXRot(), entity.getYRot());
				}

				Vec3 start = Vec3Argument.getVec3(ctx, "position");

				if (PortalCubedCommands.hasArgument(ctx, "facing")) {
					Direction facing = DirectionArgumentType.getDirection(ctx, "facing");
					return new Source(
							start,
							switch (facing) {
								case UP -> -90;
								case DOWN -> 90;
								default -> 0;
							},
							facing.toYRot()
					);
				} else {
					Coordinates coords = RotationArgument.getRotation(ctx, "rotation");
					Vec2 rotations = coords.getRotation(ctx.getSource());
					return new Source(start, rotations.x, rotations.y);
				}
			}
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
