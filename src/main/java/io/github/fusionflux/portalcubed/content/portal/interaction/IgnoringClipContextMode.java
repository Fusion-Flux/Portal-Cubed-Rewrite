package io.github.fusionflux.portalcubed.content.portal.interaction;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.raycast.NoneClipContextMode;
import io.github.fusionflux.portalcubed.mixin.utils.accessors.ClipContext$BlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * A {@link ClipContext.Block} mode that wraps another mode, excluding a specific block pos.
 * <p>
 * Constructing enum instances dynamically at runtime. Surely no problems will arise from this :clueless:
 */
public final class IgnoringClipContextMode {
	public static final String NAME = "PORTALCUBED$GENERATED$IGNORING";
	// if this value shows up in a crash report hopefully someone will realize Shenanigans are afoot
	public static final int ORDINAL = -123_456_789;

	public static ClipContext.Block create(ClipContext.Block wrapped, BlockPos posToIgnore) {
		if (wrapped == NoneClipContextMode.get())
			return wrapped;

		return ClipContext$BlockAccessor.pc$create(NAME, ORDINAL, (state, level, pos, context) -> {
			if (pos.equals(posToIgnore))
				return Shapes.empty();

			return wrapped.get(state, level, pos, context);
		});
	}

	public static ClipContext.Block maybeWrap(ClipContext.Block mode, @Nullable BlockPos posToIgnore) {
		return posToIgnore == null ? mode : create(mode, posToIgnore);
	}
}
