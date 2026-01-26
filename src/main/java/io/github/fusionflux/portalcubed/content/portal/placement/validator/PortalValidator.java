package io.github.fusionflux.portalcubed.content.portal.placement.validator;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;

public interface PortalValidator {
	Codec<PortalValidator> CODEC = PortalCubedRegistries.PORTAL_VALIDATOR_TYPE.byNameCodec()
			.dispatch(PortalValidator::type, Type::codec);
	StreamCodec<RegistryFriendlyByteBuf, PortalValidator> STREAM_CODEC = ByteBufCodecs.registry(PortalCubedRegistries.PORTAL_VALIDATOR_TYPE.key())
			.dispatch(PortalValidator::type, Type::streamCodec);

	boolean isValid(ServerLevel level, PortalId id, Portal portal);

	@ApiStatus.NonExtendable
	default boolean isValid(ServerLevel level, PortalReference reference) {
		return this.isValid(level, reference.id, reference.get());
	}

	Type<?> type();

	@Override
	boolean equals(Object o);

	@Override
	String toString();

	record Type<T extends PortalValidator>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec, CommandParser parser) {
	}

	@FunctionalInterface
	interface CommandParser {
		Parsed parse(StringReader reader) throws CommandSyntaxException;
	}

	@FunctionalInterface
	interface Parsed {
		PortalValidator build(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
	}
}
