package io.github.fusionflux.portalcubed.content.portal;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.Locale;
import java.util.function.Function;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DynamicOps;

import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.framework.command.argument.PolarityArgumentType;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalKeyArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.ServerLevel;

public record PortalDataAccessor(ServerPortalManager manager, DynamicOps<Tag> ops, PortalId id) implements DataAccessor {
	public static final DynamicCommandExceptionType FAILED_TO_DECODE = new DynamicCommandExceptionType(
			message -> Component.translatable("commands.data.portalcubed.portal.error.decode", message)
	);
	public static final SimpleCommandExceptionType NO_PORTAL = new SimpleCommandExceptionType(
			Component.translatable("commands.data.portalcubed.portal.error.missing")
	);
	public static final DynamicCommandExceptionType FAILED_TO_ENCODE = new DynamicCommandExceptionType(
			message -> Component.translatable("commands.data.portalcubed.portal.error.encode", message)
	);

	@Override
	public void setData(CompoundTag other) throws CommandSyntaxException {
		PortalData data = PortalData.CODEC.decode(this.ops, other).getOrThrow(FAILED_TO_DECODE::create).getFirst();
		this.manager.setPortal(this.id, data);
	}

	@Override
	public CompoundTag getData() throws CommandSyntaxException {
		PortalInstance portal = this.manager.getPortal(this.id);
		if (portal == null) {
			throw NO_PORTAL.create();
		}

		Tag tag = PortalData.CODEC.encodeStart(this.ops, portal.data).getOrThrow(FAILED_TO_ENCODE::create);
		if (tag instanceof CompoundTag compound) {
			return compound;
		} else {
			throw FAILED_TO_ENCODE.create("Not a map: " + tag);
		}
	}

	@Override
	public Component getModifiedSuccess() {
		return Component.translatable("commands.data.portalcubed.portal.modified", this.prettyId());
	}

	@Override
	public Component getPrintSuccess(Tag nbt) {
		return Component.translatable(
				"commands.data.portalcubed.portal.query", this.prettyId(), NbtUtils.toPrettyComponent(nbt)
		);
	}

	@Override
	public Component getPrintSuccess(NbtPathArgument.NbtPath path, double scale, int value) {
		return Component.translatable(
				"commands.data.portalcubed.portal.get", path.asString(), this.prettyId(), String.format(Locale.ROOT, "%.2f", scale), value
		);
	}

	private String prettyId() {
		return this.id.key() + ' ' + this.id.polarity();
	}

	public record Provider(String type) implements DataCommands.DataProvider {
		@Override
		public DataAccessor access(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
			String key = PortalKeyArgumentType.getKey(context, this.type + "Key");
			Polarity polarity = PolarityArgumentType.getPolarity(context, this.type + "Polarity");
			PortalId id = new PortalId(key, polarity);

			ServerLevel level = context.getSource().getLevel();
			ServerPortalManager manager = level.portalManager();

			if (manager.getPortal(id) == null) {
				throw NO_PORTAL.create();
			}

			DynamicOps<Tag> ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
			return new PortalDataAccessor(manager, ops, id);
		}

		@Override
		public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> builder, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> action) {
			return builder.then(
					literal("portalcubed:portal").then(
							argument(this.type + "Key", PortalKeyArgumentType.portalKey()).then(action.apply(
									argument(this.type + "Polarity", PolarityArgumentType.polarity())
							))
					)
			);
		}
	}
}
