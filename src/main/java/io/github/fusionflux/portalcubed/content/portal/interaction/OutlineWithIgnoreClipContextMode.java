package io.github.fusionflux.portalcubed.content.portal.interaction;

import io.github.fusionflux.portalcubed.mixin.utils.accessors.ClipContext$BlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.Shapes;

// surely no problems will arise from this :clueless:
public final class OutlineWithIgnoreClipContextMode {
	public static final String NAME = "PORTALCUBED$GENERATED$OUTLINE_WITH_IGNORE";
	// if this value shows up in a crash report hopefully someone will realize Shenanigans are afoot
	public static final int ORDINAL = -123_456_789;

	public static ClipContext.Block create(BlockPos posToIgnore) {
		return ClipContext$BlockAccessor.pc$create(NAME, ORDINAL, (state, level, pos, context) -> {
			if (pos.equals(posToIgnore))
				return Shapes.empty();

			return ClipContext.Block.OUTLINE.get(state, level, pos, context);
		});
	}
}
