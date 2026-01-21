package io.github.fusionflux.portalcubed.content.portal;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import net.minecraft.world.damagesource.DeathMessageType;

public final class MirrorTestDeathMessageType {
	public static final String NAME = "PORTALCUBED$MIRROR_TEST";
	public static final String SERIALIZED_NAME = "portalcubed:mirror_test";

	private static final Supplier<DeathMessageType> supplier = Suppliers.memoize(() -> DeathMessageType.valueOf(NAME));

	public static DeathMessageType get() {
		return supplier.get();
	}
}
