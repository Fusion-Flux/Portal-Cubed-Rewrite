package io.github.fusionflux.portalcubed.content.portal.placement.validator;

import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;

public enum NonePortalValidator implements PortalValidator {
	INSTANCE;

	public static final PortalValidator.Type<?> TYPE = new Type<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE), reader -> ctx -> INSTANCE);

	@Override
	public boolean isValid(ServerLevel level, PortalReference portal) {
		return true;
	}

	@Override
	public Type<?> type() {
		return TYPE;
	}

	@Override
	public String toString() {
		return "none";
	}
}
