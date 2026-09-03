package io.github.fusionflux.portalcubed.content.portal.command;

import static net.minecraft.commands.Commands.argument;

import java.util.Locale;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DynamicOps;

import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import io.github.fusionflux.portalcubed.framework.command.argument.PortalIdArgumentType;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ArgProvider;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.level.ServerLevel;

/// Provides data from portals to `/data`.
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

	public static final ArgProvider.Factory<DataAccessor> PROVIDER = arg -> ArgProvider.create(
			"portalcubed:portal",
			() -> argument(arg, PortalIdArgumentType.portalId()),
			context -> {
				PortalId id = PortalIdArgumentType.getId(context, arg);
				ServerLevel level = context.getSource().getLevel();
				ServerPortalManager manager = level.portalManager();

				if (manager.getPortal(id) == null) {
					throw NO_PORTAL.create();
				}

				DynamicOps<Tag> ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
				return new PortalDataAccessor(manager, ops, id);
			}
	);

	@Override
	public void setData(CompoundTag other) throws CommandSyntaxException {
		PortalData data = PortalData.CODEC.decode(this.ops, other).getOrThrow(FAILED_TO_DECODE::create).getFirst();
		this.manager.setPortal(this.id, data);
	}

	@Override
	public CompoundTag getData() throws CommandSyntaxException {
		PortalReference portal = this.manager.getPortal(this.id);
		if (portal == null) {
			throw NO_PORTAL.create();
		}

		Tag tag = PortalData.CODEC.encodeStart(this.ops, portal.get().data).getOrThrow(FAILED_TO_ENCODE::create);
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
}
