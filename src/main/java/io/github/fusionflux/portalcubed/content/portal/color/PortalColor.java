package io.github.fusionflux.portalcubed.content.portal.color;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;

public interface PortalColor {
	Codec<PortalColor> CODEC = PortalCubedRegistries.PORTAL_COLOR_TYPE.byNameCodec()
			.dispatch(PortalColor::type, Type::codec);
	StreamCodec<RegistryFriendlyByteBuf, PortalColor> STREAM_CODEC = ByteBufCodecs.registry(PortalCubedRegistries.PORTAL_COLOR_TYPE.key())
			.dispatch(PortalColor::type, Type::streamCodec);

	int get(float ticks);

	default int getOpaque(float ticks) {
		return ARGB.opaque(this.get(ticks));
	}

	Type<?> type();

	@Override
	boolean equals(Object o);

	// the eternal problem of too little tooltip context
	static float tryGetClientTicks() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			return getClientTicks();
		} else {
			return 0;
		}
	}

	@Environment(EnvType.CLIENT)
	static float getClientTicks() {
		return getClientTicks(Minecraft.getInstance().level);
	}

	@Environment(EnvType.CLIENT)
	static float getClientTicks(@Nullable ClientLevel level) {
		if (level == null)
			return 0;

		return level.getGameTime() + Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
	}

	record Type<T extends PortalColor>(MapCodec<T> codec, StreamCodec<ByteBuf, T> streamCodec, CommandParser parser) {
	}

	@FunctionalInterface
	interface CommandParser {
		PortalColor parse(StringReader reader) throws CommandSyntaxException;
	}
}
