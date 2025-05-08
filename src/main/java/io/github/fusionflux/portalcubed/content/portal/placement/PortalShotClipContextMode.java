package io.github.fusionflux.portalcubed.content.portal.placement;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import net.minecraft.world.level.ClipContext;

public final class PortalShotClipContextMode {
	public static final String NAME = "PORTALCUBED$PORTAL_SHOT";
	public static final ClipContext.ShapeGetter SHAPE_GETTER = PortalBumper::getPortalVisibleShape;

	private static final Supplier<ClipContext.Block> supplier = Suppliers.memoize(() -> ClipContext.Block.valueOf(NAME));

	public static ClipContext.Block get() {
		return supplier.get();
	}
}
